package com.proyecto.microclientes.service;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository repo;

    public List<Cliente> listar() { return repo.findAll(); }
    public Cliente buscarPorId(String clienteid) { return repo.findById(clienteid).orElseThrow(); }
    public Cliente guardar(Cliente c) { return repo.save(c); }
    public void eliminar(String clienteid) { repo.deleteById(clienteid); }
} 