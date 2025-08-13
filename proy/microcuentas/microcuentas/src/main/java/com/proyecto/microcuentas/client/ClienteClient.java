package com.proyecto.microcuentas.client;

import com.proyecto.microcuentas.dto.ClienteDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ClienteClient {
    
    private final WebClient webClient;
    
    public ClienteClient(WebClient webClient) {
        this.webClient = webClient;
    }
    
    /**
     * Obtiene un cliente por clienteid usando WebFlux (asíncrono)
     * Se bloquea para mantener API síncrona hacia arriba
     */
    public ClienteDTO obtenerCliente(String clienteid) {
        log.info("Obteniendo cliente con ID: {}", clienteid);
        try {
            return webClient
                .get()
                .uri("/clientes/{clienteid}", clienteid)
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .block(); // Convierte a síncrono
        } catch (Exception e) {
            log.error("Error al obtener cliente con ID: {}", clienteid, e);
            throw new RuntimeException("Cliente no encontrado: " + clienteid);
        }
    }
    
    /**
     * Obtiene un cliente por identificación usando WebFlux (asíncrono)
     * Se bloquea para mantener API síncrona hacia arriba
     */
    public ClienteDTO obtenerClientePorIdentificacion(String identificacion) {
        log.info("Obteniendo cliente con identificación: {}", identificacion);
        try {
            return webClient
                .get()
                .uri("/clientes/identificacion/{identificacion}", identificacion)
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .block(); // Convierte a síncrono
        } catch (Exception e) {
            log.error("Error al obtener cliente con identificación: {}", identificacion, e);
            throw new RuntimeException("Cliente no encontrado: " + identificacion);
        }
    }
    
    /**
     * Método asíncrono puro para uso interno (WebFlux)
     */
    public Mono<ClienteDTO> obtenerClienteAsync(String clienteid) {
        log.info("Obteniendo cliente async con ID: {}", clienteid);
        return webClient
            .get()
            .uri("/clientes/{clienteid}", clienteid)
            .retrieve()
            .bodyToMono(ClienteDTO.class);
    }
    
    /**
     * Método asíncrono puro para uso interno (WebFlux)
     */
    public Mono<ClienteDTO> obtenerClientePorIdentificacionAsync(String identificacion) {
        log.info("Obteniendo cliente async con identificación: {}", identificacion);
        return webClient
            .get()
            .uri("/clientes/identificacion/{identificacion}", identificacion)
            .retrieve()
            .bodyToMono(ClienteDTO.class);
    }
} 