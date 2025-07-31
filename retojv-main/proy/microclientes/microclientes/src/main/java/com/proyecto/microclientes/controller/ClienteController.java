package com.proyecto.microclientes.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.microclientes.dto.ClienteDTO;
import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;
    private final ModelMapper mapper;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar() {
        List<ClienteDTO> clientes = service.listar().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{clienteid}")
    public ResponseEntity<ClienteDTO> buscar(@PathVariable String clienteid) {
        ClienteDTO cliente = toDTO(service.buscarPorId(clienteid));
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/identificacion/{identificacion}")
    public ResponseEntity<ClienteDTO> buscarPorIdentificacion(@PathVariable String identificacion) {
        ClienteDTO cliente = toDTO(service.buscarPorIdentificacion(identificacion));
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO dto) {
        Cliente cliente = toEntity(dto);
        ClienteDTO clienteGuardado = toDTO(service.guardar(cliente));
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteGuardado);
    }

    @PutMapping("/{clienteid}")
    public ResponseEntity<ClienteDTO> actualizar(@PathVariable String clienteid, @Valid @RequestBody ClienteDTO dto) {
        Cliente cliente = toEntity(dto);
        cliente.setClienteid(clienteid);
        ClienteDTO clienteActualizado = toDTO(service.guardar(cliente));
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{clienteid}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteid) {
        service.eliminar(clienteid);
        return ResponseEntity.noContent().build();
    }

    private ClienteDTO toDTO(Cliente cliente) {
        return mapper.map(cliente, ClienteDTO.class);
    }

    private Cliente toEntity(ClienteDTO dto) {
        return mapper.map(dto, Cliente.class);
    }
} 