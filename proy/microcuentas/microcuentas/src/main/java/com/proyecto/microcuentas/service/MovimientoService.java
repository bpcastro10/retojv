package com.proyecto.microcuentas.service;

import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.MovimientoRepository;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.exception.SaldoInsuficienteException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoService {
    
    private final MovimientoRepository movRepo;
    private final CuentaRepository cuentaRepo;


    @Transactional
    public Movimiento crearMovimiento(Movimiento mov) {
        log.info("Creando nuevo movimiento para cuenta: {}", mov.getCuenta().getNumeroCuenta());
        
        // Datos validados en DTO
        
        // Obtener la cuenta
        Cuenta cuenta = cuentaRepo.findById(mov.getCuenta().getNumeroCuenta())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        
        // Normalizar valor según tipo de movimiento
        BigDecimal valor = mov.getValor();
        String tipoMovimiento = mov.getTipoMovimiento().toUpperCase();
        
        if ("DEBITO".equals(tipoMovimiento)) {
            // Para DÉBITO, asegurar que el valor sea negativo
            if (valor.compareTo(BigDecimal.ZERO) > 0) {
                valor = valor.negate();
            }
        } else if ("CREDITO".equals(tipoMovimiento)) {
            // Para CRÉDITO, asegurar que el valor sea positivo
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                valor = valor.abs();
            }
        }
        
        // Actualizar el valor normalizado
        mov.setValor(valor);
        
        // Calcular nuevo saldo
        BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(valor);
        
        // Validar saldo suficiente para débitos
        if ("DEBITO".equals(tipoMovimiento) && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException("Saldo no disponible");
        }
        
        // Actualizar saldo de la cuenta
        cuenta.setSaldoInicial(nuevoSaldo);
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuentaRepo.save(cuenta);
        
        // Configurar el movimiento
        mov.setSaldo(nuevoSaldo);
        mov.setFecha(LocalDateTime.now());
        return movRepo.save(mov);
    }

    public List<Movimiento> reportePorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de movimientos entre {} y {}", fechaInicio, fechaFin);
        
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }
        return movRepo.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    public List<Movimiento> reportePorCliente(String numeroCuenta) {
        log.info("Generando reporte de movimientos para cuenta: {}", numeroCuenta);
        
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede estar vacío");
        }
        return movRepo.findByCuentaNumeroCuenta(numeroCuenta);
    }

    public Movimiento obtenerPorId(Long id) {
        log.info("Obteniendo movimiento con ID: {}", id);
        return movRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));
    }

    public List<Movimiento> obtenerTodos() {
        log.info("Obteniendo todos los movimientos");
        return movRepo.findAll();
    }
} 