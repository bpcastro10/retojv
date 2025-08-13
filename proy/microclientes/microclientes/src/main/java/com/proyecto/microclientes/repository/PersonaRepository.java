package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, String> {
    
    /**
     * Busca una persona por nombre (case insensitive)
     * @param nombre nombre a buscar
     * @return Lista de personas que coinciden
     */
    Optional<Persona> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca personas por género
     * @param genero género a buscar ('M' o 'F')
     * @return Lista de personas del género especificado
     */
    java.util.List<Persona> findByGenero(String genero);
    
    /**
     * Busca personas por rango de edad
     * @param edadMin edad mínima
     * @param edadMax edad máxima
     * @return Lista de personas en el rango de edad
     */
    java.util.List<Persona> findByEdadBetween(Integer edadMin, Integer edadMax);
}