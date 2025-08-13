package com.proyecto.microcuentas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.CuentaRepository;

import com.proyecto.microcuentas.client.ClienteClient;
import com.proyecto.microcuentas.dto.ClienteDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaService {
    
    private final CuentaRepository cuentaRepository;

    private final ClienteClient clienteClient;

    public List<Cuenta> obtenerTodasLasCuentas() {
        log.info("Obteniendo todas las cuentas");
        return cuentaRepository.findAll();
    }

    public Optional<Cuenta> obtenerCuentaPorId(String numeroCuenta) {
        log.info("Obteniendo cuenta con número: {}", numeroCuenta);
        return cuentaRepository.findById(numeroCuenta);
    }

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {
        log.info("Creando nueva cuenta: {} para cliente: {}", cuenta.getNumeroCuenta(), cuenta.getClienteId());
        
        // 1. Validar que el cliente existe y está activo usando WebFlux
        validarClienteExisteYActivo(cuenta.getClienteId());
        
        // 2. Configurar fechas
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setFechaActualizacion(LocalDateTime.now());
        
        // 3. Datos validados en DTO
        
        // 4. Guardar la cuenta
        Cuenta cuentaCreada = cuentaRepository.save(cuenta);
        
        log.info("Cuenta {} creada exitosamente para cliente {}", 
                 cuentaCreada.getNumeroCuenta(), cuentaCreada.getClienteId());
        
        return cuentaCreada;
    }
    
    /**
     * Valida que el cliente existe y está en estado ACTIVO usando WebFlux
     */
    private void validarClienteExisteYActivo(String clienteId) {
        try {
            log.info("Validando cliente con ID: {}", clienteId);
            
            // Comunicación con microclientes usando WebFlux
            ClienteDTO cliente = clienteClient.obtenerClientePorIdentificacion(clienteId);
            
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente no encontrado: " + clienteId);
            }
            
            if (!"ACTIVO".equals(cliente.getEstado())) {
                throw new IllegalArgumentException("Cliente no está activo. Estado actual: " + cliente.getEstado());
            }
            
            log.info("Cliente {} validado exitosamente - Estado: {}", 
                     cliente.getNombre(), cliente.getEstado());
                     
        } catch (Exception e) {
            log.error("Error al validar cliente {}: {}", clienteId, e.getMessage());
            throw new IllegalArgumentException("No se puede crear cuenta. Cliente " + clienteId + " no existe o no está activo: " + e.getMessage());
        }
    }

    @Transactional
    public Cuenta actualizarCuenta(String numeroCuenta, Cuenta cuentaActualizada) {
        log.info("Actualizando cuenta: {}", numeroCuenta);
        Optional<Cuenta> cuentaExistente = cuentaRepository.findById(numeroCuenta);
        if (cuentaExistente.isPresent()) {
            Cuenta cuenta = cuentaExistente.get();
            cuenta.setTipoCuenta(cuentaActualizada.getTipoCuenta());
            cuenta.setSaldoInicial(cuentaActualizada.getSaldoInicial());
            cuenta.setEstado(cuentaActualizada.getEstado());
            cuenta.setFechaActualizacion(LocalDateTime.now());

            return cuentaRepository.save(cuenta);
        }
        throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
    }

    @Transactional
    public void eliminarCuenta(String numeroCuenta) {
        log.info("Eliminando cuenta: {}", numeroCuenta);
        if (!cuentaRepository.existsById(numeroCuenta)) {
            throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
        }
        cuentaRepository.deleteById(numeroCuenta);
    }

    @Transactional
    public Cuenta actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) {
        log.info("Actualizando saldo de cuenta: {} a {}", numeroCuenta, nuevoSaldo);
        Optional<Cuenta> cuentaExistente = cuentaRepository.findById(numeroCuenta);
        if (cuentaExistente.isPresent()) {
            Cuenta cuenta = cuentaExistente.get();
            cuenta.setSaldoInicial(nuevoSaldo);
            cuenta.setFechaActualizacion(LocalDateTime.now());
            return cuentaRepository.save(cuenta);
        }
        throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
    }
    
    /**
     * Obtener todas las cuentas de un cliente específico
     */
    public List<Cuenta> obtenerCuentasPorCliente(String clienteId) {
        log.info("Obteniendo cuentas para cliente: {}", clienteId);
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        log.info("Encontradas {} cuentas para cliente {}", cuentas.size(), clienteId);
        return cuentas;
    }
    
    /**
     * Obtener cuentas activas de un cliente
     */
    public List<Cuenta> obtenerCuentasActivasPorCliente(String clienteId) {
        log.info("Obteniendo cuentas activas para cliente: {}", clienteId);
        List<Cuenta> cuentas = cuentaRepository.findByClienteIdAndEstado(clienteId, "ACTIVA");
        log.info("Encontradas {} cuentas activas para cliente {}", cuentas.size(), clienteId);
        return cuentas;
    }
    
    /**
     * Verificar si un cliente tiene al menos una cuenta activa
     */
    public boolean clienteTieneCuentaActiva(String clienteId) {
        log.info("Verificando si cliente {} tiene cuenta activa", clienteId);
        boolean tieneActiva = cuentaRepository.existeCuentaActivaPorCliente(clienteId);
        log.info("Cliente {} {} cuenta activa", clienteId, tieneActiva ? "tiene" : "no tiene");
        return tieneActiva;
    }
} 