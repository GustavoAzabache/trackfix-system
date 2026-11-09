package com.me.backend_springboot.customer.service;

import org.springframework.stereotype.Service;

import com.me.backend_springboot.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Como la información del cliente se guardará junto con la orden, tal vez no se necesite endpoints
    // Como no habrá un control del tipo: "buscar al usuario que anteriormente hizo una orden y asignarlo nuevalmente",
    // se usará lógica de upsert al momento de guardar los datos. y se tiene que normalizar todo al momento de ingresar
    // Como para guardar a un cliente se requiere o el email o el celular o ambos, la condición del upsert se evaluará
    // en base a estos campos

    public void save() {

    }

}
