package com.proyecto.microcuentas.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<?> handleSaldoInsuficienteException(SaldoInsuficienteException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Saldo Insuficiente");
        response.put("mensaje", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Error de Validación");
        response.put("mensaje", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Error del Sistema");
        response.put("mensaje", "Ha ocurrido un error inesperado");
        return ResponseEntity.internalServerError().body(response);
    }
} 