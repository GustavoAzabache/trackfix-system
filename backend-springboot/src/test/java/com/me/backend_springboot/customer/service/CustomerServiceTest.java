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

import com.me.backend_springboot.customer.dto.request.CreateCustomerRequest;
import com.me.backend_springboot.customer.entity.CustomerEntity;
import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.customer.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private static CreateCustomerRequest request(
            String name, String lastName, String dni, String phone, String email) {
        return new CreateCustomerRequest(name, lastName, dni, phone, email);
    }

    @Test
    @DisplayName("Cliente nuevo: findByDni vacío → create + save y devuelve la entidad")
    void upsertCreaClienteNuevo() {
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", "987654321", null);

        when(customerRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Brandon");
        assertThat(result.getDni()).isEqualTo("12345678");
        assertThat(result.getPhone()).isEqualTo("987654321");
        assertThat(result.getEmail()).isNull();
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    @DisplayName("Normalización antes de buscar: DNI/correo con espacios → findByDni con el DNI limpio")
    void normalizaAntesDeBuscar() {
        CreateCustomerRequest req = request(
            "Brandon", "Quispe", " 12345678 ", "987654321", " BRANDON@Trackfix.COM "
        );

        when(customerRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req);

        verify(customerRepository).findByDni("12345678"); // sin espacios: la consulta encuentra el registro
        assertThat(result.getDni()).isEqualTo("12345678");
        assertThat(result.getEmail()).isEqualTo("brandon@trackfix.com");
    }

    @Test
    @DisplayName("Cliente existente: update parcial conserva el canal de contacto que no vino")
    void upsertActualizaYConservaContacto() {
        CustomerEntity existing = CustomerEntity.create(
            "Brandon", "Quispe", "12345678", null, "987654321"
        );
        // La petición trae SOLO correo → el celular anterior se conserva
        CreateCustomerRequest req = request(
            "Brandon", "Quispe", "12345678", null, "brandon@trackfix.com"
        );

        when(customerRepository.findByDni("12345678")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerEntity result = customerService.upsert(req);

        assertThat(result.getEmail()).isEqualTo("brandon@trackfix.com");
        assertThat(result.getPhone()).isEqualTo("987654321"); // conservado
        assertThat(result.getDni()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Petición sin ningún canal de contacto: se rechaza en el servicio antes de tocar la BD")
    void upsertRechazaSinContacto() {
        // La regla es por petición: SIEMPRE debe venir una vía de contacto
        // (es la que se usa para mandarle el código de rastreo)
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345678", null, null);

        assertThatThrownBy(() -> customerService.upsert(req))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("canal de contacto");

        verify(customerRepository, never()).findByDni(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("DNI con formato inválido: se rechaza sin guardar")
    void upsertRechazaDniInvalido() {
        CreateCustomerRequest req = request("Brandon", "Quispe", "12345", "987654321", null);

        when(customerRepository.findByDni("12345")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.upsert(req))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("8 dígitos");

        verify(customerRepository, never()).save(any());
    }

}
