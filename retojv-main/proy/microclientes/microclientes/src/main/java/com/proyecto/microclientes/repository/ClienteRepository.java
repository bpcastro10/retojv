package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByIdentificacion(String identificacion);
} 