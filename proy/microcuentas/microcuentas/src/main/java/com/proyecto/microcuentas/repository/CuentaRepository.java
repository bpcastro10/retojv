package com.proyecto.microcuentas.repository;

import com.proyecto.microcuentas.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    
    /**
     * Buscar todas las cuentas de un cliente específico
     */
    List<Cuenta> findByClienteId(String clienteId);
    
    /**
     * Buscar cuentas activas de un cliente
     */
    List<Cuenta> findByClienteIdAndEstado(String clienteId, String estado);
    
    /**
     * Contar cuentas por cliente
     */
    Long countByClienteId(String clienteId);
    
    /**
     * Verificar si existe al menos una cuenta activa para un cliente
     */
    @Query("SELECT COUNT(c) > 0 FROM Cuenta c WHERE c.clienteId = :clienteId AND c.estado = 'ACTIVA'")
    boolean existeCuentaActivaPorCliente(@Param("clienteId") String clienteId);
} 