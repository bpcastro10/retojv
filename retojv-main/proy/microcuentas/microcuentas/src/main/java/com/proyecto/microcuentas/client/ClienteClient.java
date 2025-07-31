package com.proyecto.microcuentas.client;

import com.proyecto.microcuentas.dto.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microclientes")
public interface ClienteClient {
    
    @GetMapping("/clientes/{clienteid}")
    ClienteDTO obtenerCliente(@PathVariable("clienteid") String clienteid);
    
    @GetMapping("/clientes/identificacion/{identificacion}")
    ClienteDTO obtenerClientePorIdentificacion(@PathVariable("identificacion") String identificacion);
} 