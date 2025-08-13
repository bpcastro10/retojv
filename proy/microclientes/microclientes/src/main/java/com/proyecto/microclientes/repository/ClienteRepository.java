package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    
    /**
     * Busca un cliente por clienteid
     * @param clienteid ID único del cliente
     * @return Optional con el cliente encontrado
     */
    Optional<Cliente> findByClienteid(String clienteid);
    
    /**
     * Busca clientes por estado
     * @param estado estado del cliente ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')
     * @return Lista de clientes con el estado especificado
     */
    List<Cliente> findByEstado(String estado);
    
    /**
     * Busca clientes por nombre (usando JOIN con la tabla persona)
     * @param nombre nombre a buscar
     * @return Lista de clientes que coinciden
     */
    @Query("SELECT c FROM Cliente c WHERE c.nombre LIKE %:nombre%")
    List<Cliente> findByNombreContaining(@Param("nombre") String nombre);
    
    /**
     * Cuenta clientes activos
     * @return número de clientes activos
     */
    Long countByEstado(String estado);
} 