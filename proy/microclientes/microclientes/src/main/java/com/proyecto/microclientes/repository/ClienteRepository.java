package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {} 