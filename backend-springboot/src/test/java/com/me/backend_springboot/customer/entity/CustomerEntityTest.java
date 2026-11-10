package com.me.backend_springboot.customer.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.me.backend_springboot.auth.entity.AuthEntity;
import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.tenant.entity.TenantEntity;

class CustomerEntityTest {

    private static final String NAME = "Brandon";
    private static final String LAST_NAME = "Quispe";
    private static final String DNI = "12345678";
    private static final String EMAIL = "brandon@trackfix.com";
    private static final String PHONE = "987654321";

    // El taller al que pertenece el cliente (RF-03): instancia real, sin mocks
    private static final TenantEntity TENANT = TenantEntity.create(
        "ABCD",
        "Taller TrackFix",
        "987654321",
        "taller@trackfix.com",
        null,
        "Av. Principal 123, Lince, Lima",
        AuthEntity.create("dueno@trackfix.com", "hash-secret")
    );

    // ---------- create ----------

    @Test
    @DisplayName("create válida: guarda los campos, los normaliza y marca las fechas")
    void createValidaGuardaYNormaliza() {
        CustomerEntity customer = CustomerEntity.create(
            "  Brandon  ", " Quispe ", " 12345678 ", " BRANDON@Trackfix.COM ", " 987 654 321 ", TENANT
        );

        assertThat(customer.getName()).isEqualTo("Brandon");
        assertThat(customer.getLastName()).isEqualTo("Quispe");
        assertThat(customer.getDni()).isEqualTo("12345678");
        assertThat(customer.getEmail()).isEqualTo("brandon@trackfix.com");
        assertThat(customer.getPhone()).isEqualTo("987654321");
        assertThat(customer.getTenant()).isSameAs(TENANT);
        assertThat(customer.getCreatedAt()).isEqualTo(customer.getUpdatedAt());
    }

    @Test
    @DisplayName("create sin taller: se rechaza (RF-03: el cliente siempre pertenece a un taller)")
    void createSinTenantSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, DNI, EMAIL, PHONE, null))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("El taller es obligatorio");
    }

    @Test
    @DisplayName("name/lastName se guardan tal cual: solo trim, sin tocar mayúsculas (brandom es brandom)")
    void nameSinNormalizarSoloTrim() {
        CustomerEntity customer = CustomerEntity.create("brandom", "QUIsPE", DNI, EMAIL, PHONE, TENANT);

        assertThat(customer.getName()).isEqualTo("brandom");
        assertThat(customer.getLastName()).isEqualTo("QUIsPE");
    }

    @Test
    @DisplayName("create sin ningún canal de contacto: se rechaza")
    void createSinContactoSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, DNI, null, null, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("canal de contacto");
    }

    @Test
    @DisplayName("create sin DNI: se rechaza")
    void createSinDniSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, null, EMAIL, PHONE, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("DNI es obligatorio");
    }

    @Test
    @DisplayName("create con DNI de 7 dígitos: se rechaza")
    void createConDniCortoSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, "1234567", EMAIL, PHONE, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("8 dígitos");
    }

    @Test
    @DisplayName("create con celular de 8 dígitos: se rechaza")
    void createConPhoneCortoSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, DNI, null, "98765432", TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("celular");
    }

    @Test
    @DisplayName("create con celular que no empieza con 9: se rechaza")
    void createConPhoneQueNoEmpiezaCon9SeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, DNI, null, "876543210", TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("celular");
    }

    @Test
    @DisplayName("create con correo inválido: se rechaza")
    void createConEmailInvalidoSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, LAST_NAME, DNI, "no-es-un-correo", PHONE, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("create sin nombre: se rechaza")
    void createSinNombreSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create("   ", LAST_NAME, DNI, EMAIL, PHONE, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("nombre es obligatorio");
    }

    @Test
    @DisplayName("create sin apellido: se rechaza")
    void createSinApellidoSeRechaza() {
        assertThatThrownBy(() -> CustomerEntity.create(NAME, "", DNI, EMAIL, PHONE, TENANT))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("apellido es obligatorio");
    }

    // ---------- update ----------

    @Test
    @DisplayName("update parcial con solo celular: conserva el correo y el nombre")
    void updateParcialConservaLoQueNoVino() {
        CustomerEntity customer = CustomerEntity.create(NAME, LAST_NAME, DNI, EMAIL, PHONE, TENANT);

        customer.update(null, null, null, "911222333");

        assertThat(customer.getPhone()).isEqualTo("911222333");
        assertThat(customer.getEmail()).isEqualTo(EMAIL);   // conservado
        assertThat(customer.getName()).isEqualTo(NAME);     // conservado
        assertThat(customer.getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    @DisplayName("update con solo correo: conserva el celular")
    void updateSoloEmailConservaPhone() {
        CustomerEntity customer = CustomerEntity.create(NAME, LAST_NAME, DNI, null, PHONE, TENANT);

        customer.update(NAME, LAST_NAME, "nuevo@trackfix.com", null);

        assertThat(customer.getEmail()).isEqualTo("nuevo@trackfix.com");
        assertThat(customer.getPhone()).isEqualTo(PHONE);   // conservado
    }

    @Test
    @DisplayName("update con celular inválido: se rechaza y la entidad queda intacta")
    void updateConPhoneInvalidoNoTocaNada() {
        CustomerEntity customer = CustomerEntity.create(NAME, LAST_NAME, DNI, EMAIL, PHONE, TENANT);

        assertThatThrownBy(() -> customer.update(null, null, null, "12345"))
            .isInstanceOf(CustomerDataException.class)
            .hasMessageContaining("celular");

        assertThat(customer.getPhone()).isEqualTo(PHONE);   // intacto
        assertThat(customer.getEmail()).isEqualTo(EMAIL);
        assertThat(customer.getName()).isEqualTo(NAME);
    }

    @Test
    @DisplayName("update no toca DNI, taller ni fechas: updatedAt lo refresca @PreUpdate en el flush, no update()")
    void updateNoTocaDniNiFechas() {
        CustomerEntity customer = CustomerEntity.create(NAME, LAST_NAME, DNI, EMAIL, PHONE, TENANT);
        LocalDateTime createdAt = customer.getCreatedAt();
        LocalDateTime updatedAt = customer.getUpdatedAt();

        customer.update("Brandon Alonso", LAST_NAME, "otro@trackfix.com", "911222333");

        assertThat(customer.getDni()).isEqualTo(DNI);             // identidad inmutable
        assertThat(customer.getTenant()).isSameAs(TENANT);        // el cliente no cambia de taller
        assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
        assertThat(customer.getUpdatedAt()).isEqualTo(updatedAt); // no lo toca update()
        assertThat(customer.getName()).isEqualTo("Brandon Alonso");
    }

}
