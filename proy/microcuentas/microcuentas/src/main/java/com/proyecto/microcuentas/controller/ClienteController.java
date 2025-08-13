package com.proyecto.microcuentas.controller;

import com.proyecto.microcuentas.dto.ClienteDTO;
import com.proyecto.microcuentas.client.ClienteClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Controlador para operaciones CRUD de clientes
 * Actúa como proxy hacia el microservicio microclientes usando WebFlux
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {
    
    private final ClienteClient clienteClient;

    /**
     * Obtener cliente por ID (delegado al microservicio microclientes)
     */
    @GetMapping("/{clienteid}")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable String clienteid) {
        log.info("Obteniendo cliente con ID: {}", clienteid);
        try {
            ClienteDTO cliente = clienteClient.obtenerCliente(clienteid);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            log.error("Error al obtener cliente: {}", clienteid, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener cliente por identificación (delegado al microservicio microclientes)
     */
    @GetMapping("/identificacion/{identificacion}")
    public ResponseEntity<ClienteDTO> obtenerPorIdentificacion(@PathVariable String identificacion) {
        log.info("Obteniendo cliente con identificación: {}", identificacion);
        try {
            ClienteDTO cliente = clienteClient.obtenerClientePorIdentificacion(identificacion);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            log.error("Error al obtener cliente por identificación: {}", identificacion, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * NOTA: Para operaciones de creación, actualización y eliminación,
     * se debe usar directamente el microservicio microclientes.
     * Este controlador proporciona solo consultas para el contexto de microcuentas.
     * 
     * Endpoints completos de clientes disponibles en:
     * - http://localhost:8080/clientes (microservicio microclientes)
     */
}