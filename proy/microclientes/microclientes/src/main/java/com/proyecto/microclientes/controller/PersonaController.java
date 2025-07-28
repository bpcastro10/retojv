package com.proyecto.microclientes.controller;

import com.proyecto.microclientes.entity.Persona;
import com.proyecto.microclientes.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/personas")
public class PersonaController {
    @Autowired
    private PersonaRepository personaRepository;

    @GetMapping
    public List<Persona> listar() {
        return personaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Persona obtener(@PathVariable String id) {
        return personaRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Persona crear(@RequestBody Persona persona) {
        return personaRepository.save(persona);
    }

    @PutMapping("/{id}")
    public Persona actualizar(@PathVariable String id, @RequestBody Persona persona) {
        persona.setIdentificacion(id);
        return personaRepository.save(persona);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        personaRepository.deleteById(id);
    }
} 