package com.proyecto.microclientes.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Cliente {
    @Id
    private String clienteid;
    private String contrasena;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "identificacion", referencedColumnName = "identificacion")
    private Persona persona;

    public String getClienteid() { return clienteid; }
    public void setClienteid(String clienteid) { this.clienteid = clienteid; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
} 