package com.me.backend_springboot.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.me.backend_springboot.auth.entity.AuthEntity;
import com.me.backend_springboot.customer.dto.request.CreateCustomerRequest;
import com.me.backend_springboot.customer.entity.CustomerEntity;
import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.customer.repository.CustomerRepository;
import com.me.backend_springboot.tenant.entity.TenantEntity;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // El taller de la sesión: el upsert SIEMPRE busca y crea dentro de este taller
    private final TenantEntity tenant = tenant("ABCD", "Taller TrackFix");

    private static TenantEntity tenant(String code, String name) {
        String slug = code.toLowerCase();

        return TenantEntity.create(
            code,
            name,
            "987654321",
            "contacto@" + slug + ".com",
            null,
            "Av. Principal 123, Lince, Lima",
            AuthEntity.create("dueno@" + slug + ".com", "hash-secret")
        );
    }

    private static CreateCustomerRequest request(
            String name, String lastName, String dni, String phone, String email) {
        return new CreateCustomerRequest(name, lastName, dni, phone, email);
    }

    @Test
    @DisplayName("Cliente nuevo: findByDniAndTenant vacío → create + save con el taller de la sesión")
    void upsertCreaClienteNuevo() {
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", "987654321", null);

        when(customerRepository.findByDniAndTenant("12345678", tenant)).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req, tenant);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Brandon");
        assertThat(result.getDni()).isEqualTo("12345678");
        assertThat(result.getPhone()).isEqualTo("987654321");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getTenant()).isSameAs(tenant);
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    @DisplayName("Normalización antes de buscar: DNI/correo con espacios → findByDniAndTenant con DNI limpio y el taller")
    void normalizaAntesDeBuscar() {
        CreateCustomerRequest req = request(
            "Brandon", "Quispe", " 12345678 ", "987654321", " BRANDON@Trackfix.COM "
        );

        when(customerRepository.findByDniAndTenant("12345678", tenant)).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req, tenant);

        // La consulta siempre lleva el DNI limpio Y el taller de la sesión
        verify(customerRepository).findByDniAndTenant("12345678", tenant);
        assertThat(result.getDni()).isEqualTo("12345678");
        assertThat(result.getEmail()).isEqualTo("brandon@trackfix.com");
    }

    @Test
    @DisplayName("Cliente existente en ESTE taller: update parcial conserva el canal de contacto que no vino")
    void upsertActualizaYConservaContacto() {
        CustomerEntity existing = CustomerEntity.create(
            "Brandon", "Quispe", "12345678", null, "987654321", tenant
        );
        // La petición trae SOLO correo → el celular anterior se conserva
        CreateCustomerRequest req = request(
            "Brandon", "Quispe", "12345678", null, "brandon@trackfix.com"
        );

        when(customerRepository.findByDniAndTenant("12345678", tenant)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req, tenant);

        assertThat(result.getEmail()).isEqualTo("brandon@trackfix.com");
        assertThat(result.getPhone()).isEqualTo("987654321"); // conservado
        assertThat(result.getDni()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Mismo DNI en otro taller: no lo encuentra y crea el cliente en ESTE taller (RF-03)")
    void upsertNoCruzaTalleres() {
        TenantEntity otroTaller = tenant("WXYZ", "Otro Taller");
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", "987654321", null);

        // El DNI ya existe en otroTaller, pero la búsqueda es por (dni, tenant)
        when(customerRepository.findByDniAndTenant("12345678", tenant)).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req, tenant);

        verify(customerRepository).findByDniAndTenant("12345678", tenant);
        verify(customerRepository, never()).findByDniAndTenant("12345678", otroTaller);
        assertThat(result.getTenant()).isSameAs(tenant);
    }

    @Test
    @DisplayName("Petición sin ningún canal de contacto: se rechaza en el servicio antes de tocar la BD")
    void upsertRechazaSinContacto() {
        // La regla es por petición: SIEMPRE debe venir una vía de contacto
        // (es la que se usa para mandarle el código de rastreo)
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", null, null);

        assertThatThrownBy(() -> customerService.upsert(req, tenant))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("canal de contacto");

        verify(customerRepository, never()).findByDniAndTenant(any(), any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sin taller: se rechaza sin tocar la BD (el tenant no viene en el body, es de la sesión)")
    void upsertSinTenantSeRechaza() {
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", "987654321", null);

        assertThatThrownBy(() -> customerService.upsert(req, null))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("El taller es obligatorio");

        verify(customerRepository, never()).findByDniAndTenant(any(), any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("DNI con formato inválido: se rechaza sin guardar")
    void upsertRechazaDniInvalido() {
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345", "987654321", null);

        when(customerRepository.findByDniAndTenant("12345", tenant)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.upsert(req, tenant))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("8 dígitos");

        verify(customerRepository, never()).save(any());
    }

}
