package com.me.backend_springboot.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.employee.entity.EmployeeEntity;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

}
