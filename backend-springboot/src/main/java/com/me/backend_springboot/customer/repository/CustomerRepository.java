package com.me.backend_springboot.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.customer.entity.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

}
