package com.proyecto.microclientes.dto;

public class ClienteDTO {
    private String clienteid;
    private String contrasena;
    private String estado;
    private PersonaDTO persona;

    public String getClienteid() { return clienteid; }
    public void setClienteid(String clienteid) { this.clienteid = clienteid; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public PersonaDTO getPersona() { return persona; }
    public void setPersona(PersonaDTO persona) { this.persona = persona; }
} 