package com.proyecto.microcuentas.service;

import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.MovimientoRepository;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.exception.SaldoInsuficienteException;
import com.proyecto.microcuentas.validation.MovimientoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Service
public class MovimientoService {
    @Autowired
    private MovimientoRepository movRepo;
    
    @Autowired
    private CuentaRepository cuentaRepo;

    @Autowired
    private MovimientoValidator movimientoValidator;

    @Transactional
    public Movimiento crearMovimiento(Movimiento mov) {
        // Validar el movimiento
        movimientoValidator.validarMovimiento(mov);
        
        // Obtener la cuenta
        Cuenta cuenta = cuentaRepo.findById(mov.getCuenta().getNumeroCuenta())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
            
        // Calcular nuevo saldo
        BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(mov.getValor());
        
        // Actualizar saldo de la cuenta
        cuenta.setSaldoInicial(nuevoSaldo);
        cuentaRepo.save(cuenta);
        
        // Configurar el movimiento
        mov.setSaldo(nuevoSaldo);
        mov.setFecha(LocalDateTime.now());
        return movRepo.save(mov);
    }

    public List<Movimiento> reportePorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }
        return movRepo.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    public List<Movimiento> reportePorCliente(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede estar vacío");
        }
        return movRepo.findByCuentaNumeroCuenta(numeroCuenta);
    }

    public Movimiento obtenerPorId(Long id) {
        return movRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));
    }

    public List<Movimiento> obtenerTodos() {
        return movRepo.findAll();
    }
} 