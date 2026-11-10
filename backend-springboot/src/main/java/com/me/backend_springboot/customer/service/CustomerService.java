package com.me.backend_springboot.customer.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.me.backend_springboot.customer.dto.request.CreateCustomerRequest;
import com.me.backend_springboot.customer.entity.CustomerEntity;
import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

/**
 * El cliente NO tiene cuenta ni endpoints: sus datos llegan embebidos en la
 * orden y él solo consulta su estado con el código de rastreo.
 *
 * Único punto de entrada del módulo: upsert por DNI (identidad), normalizando
 * la petición antes de buscar para que la consulta encuentre el mismo registro
 * que se va a guardar.
 */
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Upsert por DNI:
     * 1. Normaliza la petición (DNI/email con trim — email además en minúsculas;
     *    phone sin espacios; name/lastName SOLO trim, sin tocar mayúsculas).
     * 2. Exige al menos un canal de contacto en la petición: es la vía para
     *    mandarle el código de rastreo (regla que el DTO no puede expresar y
     *    que se evalúa aquí, antes de tocar la BD).
     * 3. findByDni → si existe: update parcial (solo campos con dato);
     *    si no: create.
     * 4. Devuelve la entidad para que la orden la referencie; si los datos
     *    rompen una regla del dominio, create/update lanzan CustomerDataException.
     */
    @Transactional
    public CustomerEntity upsert(CreateCustomerRequest request) {
        String name = trimToNull(request.name());
        String lastName = trimToNull(request.lastName());
        String dni = trimToNull(request.dni());
        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phone());

        // "sí o sí se requiere una vía de contacto": el DTO no puede expresarlo
        if (email == null && phone == null) {
            throw new CustomerDataException(
                "Se requiere al menos un canal de contacto (teléfono o correo electrónico)."
            );
        }

        // Sin DNI no hay identidad por dónde buscar: create() lo rechaza
        Optional<CustomerEntity> existing = dni == null
            ? Optional.empty()
            : customerRepository.findByDni(dni);

        if (existing.isPresent()) {
            CustomerEntity customer = existing.get();
            customer.update(name, lastName, email, phone);
            return customerRepository.save(customer);
        }

        return customerRepository.save(
            CustomerEntity.create(name, lastName, dni, email, phone)
        );
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeEmail(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        String digits = trimmed.replaceAll("\\s+", "");
        return digits.isEmpty() ? null : digits;
    }

}
