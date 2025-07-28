package com.proyecto.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String timestamp = LocalDateTime.now().format(formatter);
        
        logger.info("=== Gateway Request ===");
        logger.info("Timestamp: {}", timestamp);
        logger.info("Method: {}", method);
        logger.info("Path: {}", path);
        logger.info("Headers: {}", request.getHeaders());
        logger.info("Remote Address: {}", request.getRemoteAddress());
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    String responseStatus = exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().toString() : "UNKNOWN";
                    logger.info("=== Gateway Response ===");
                    logger.info("Timestamp: {}", LocalDateTime.now().format(formatter));
                    logger.info("Status: {}", responseStatus);
                    logger.info("Path: {}", path);
                    logger.info("========================");
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
} 