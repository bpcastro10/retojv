package com.proyecto.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta para microservicio de clientes
                .route("clientes-service", r -> r
                        .path("/api/v1/clientes/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + "")
                                .addResponseHeader("X-Gateway-Source", "Spring-Cloud-Gateway"))
                        .uri("http://localhost:8080"))
                
                // Ruta para microservicio de cuentas
                .route("cuentas-service", r -> r
                        .path("/api/v1/cuentas/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + "")
                                .addResponseHeader("X-Gateway-Source", "Spring-Cloud-Gateway"))
                        .uri("http://localhost:8081"))
                
                // Ruta para microservicio de movimientos
                .route("movimientos-service", r -> r
                        .path("/api/v1/movimientos/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + "")
                                .addResponseHeader("X-Gateway-Source", "Spring-Cloud-Gateway"))
                        .uri("http://localhost:8081"))
                
                // Ruta para microservicio de reportes
                .route("reportes-service", r -> r
                        .path("/api/v1/reportes/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + "")
                                .addResponseHeader("X-Gateway-Source", "Spring-Cloud-Gateway"))
                        .uri("http://localhost:8081"))
                
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
} 