package com.proyecto.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> routes = new HashMap<>();
        
        Flux<Route> routeFlux = routeLocator.getRoutes();
        
        routeFlux.subscribe(route -> {
            String id = route.getId();
            String uri = route.getUri().toString();
            routes.put(id, uri);
        });
        
        response.put("gateway", "Spring Cloud Gateway");
        response.put("port", "8083");
        response.put("routes", routes);
        response.put("availableEndpoints", getAvailableEndpoints());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "Spring Cloud Gateway");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    private Map<String, String> getAvailableEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        
        // Endpoints de clientes
        endpoints.put("GET /api/v1/clientes", "Obtener todos los clientes");
        endpoints.put("GET /api/v1/clientes/{id}", "Obtener cliente por ID");
        endpoints.put("POST /api/v1/clientes", "Crear nuevo cliente");
        endpoints.put("PUT /api/v1/clientes/{id}", "Actualizar cliente");
        endpoints.put("DELETE /api/v1/clientes/{id}", "Eliminar cliente");
        
        // Endpoints de cuentas
        endpoints.put("GET /api/v1/cuentas", "Obtener todas las cuentas");
        endpoints.put("GET /api/v1/cuentas/{id}", "Obtener cuenta por ID");
        endpoints.put("POST /api/v1/cuentas", "Crear nueva cuenta");
        endpoints.put("PUT /api/v1/cuentas/{id}", "Actualizar cuenta");
        endpoints.put("DELETE /api/v1/cuentas/{id}", "Eliminar cuenta");
        
        // Endpoints de movimientos
        endpoints.put("GET /api/v1/movimientos", "Obtener todos los movimientos");
        endpoints.put("GET /api/v1/movimientos/cuenta/{numeroCuenta}", "Obtener movimientos por cuenta");
        endpoints.put("POST /api/v1/movimientos", "Crear nuevo movimiento");
        endpoints.put("GET /api/v1/movimientos/reporte", "Reporte de movimientos por fecha");
        
        // Endpoints de reportes
        endpoints.put("GET /api/v1/reportes/estado-cuenta/{numeroCuenta}", "Estado de cuenta");
        endpoints.put("GET /api/v1/reportes/movimientos", "Reporte de movimientos");
        
        return endpoints;
    }
} 