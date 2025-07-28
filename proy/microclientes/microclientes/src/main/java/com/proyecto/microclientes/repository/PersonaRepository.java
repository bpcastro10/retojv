package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, String> {} 