package com.proyecto.microclientes.controller;

import com.proyecto.microclientes.dto.ClienteDTO;
import com.proyecto.microclientes.dto.PersonaDTO;
import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.entity.Persona;
import com.proyecto.microclientes.service.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private ClienteService service;
    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public List<ClienteDTO> listar() {
        return service.listar().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/{clienteid}")
    public ClienteDTO buscar(@PathVariable String clienteid) {
        return toDTO(service.buscarPorId(clienteid));
    }

    @PostMapping
    public ClienteDTO crear(@RequestBody ClienteDTO dto) {
        Cliente c = toEntity(dto);
        return toDTO(service.guardar(c));
    }

    @PutMapping("/{clienteid}")
    public ClienteDTO actualizar(@PathVariable String clienteid, @RequestBody ClienteDTO dto) {
        Cliente c = toEntity(dto);
        c.setClienteid(clienteid);
        return toDTO(service.guardar(c));
    }

    @DeleteMapping("/{clienteid}")
    public void eliminar(@PathVariable String clienteid) {
        service.eliminar(clienteid);
    }

    private ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = mapper.map(c, ClienteDTO.class);
        if (c.getPersona() != null) {
            dto.setPersona(mapper.map(c.getPersona(), PersonaDTO.class));
        }
        return dto;
    }

    private Cliente toEntity(ClienteDTO dto) {
        Cliente c = mapper.map(dto, Cliente.class);
        if (dto.getPersona() != null) {
            c.setPersona(mapper.map(dto.getPersona(), Persona.class));
        }
        return c;
    }
} 