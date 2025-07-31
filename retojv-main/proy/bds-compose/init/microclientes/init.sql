CREATE TABLE persona_cliente (
    clienteid VARCHAR(20) PRIMARY KEY,        -- ID único del cliente
    identificacion VARCHAR(20),               -- Cédula de la persona (puede o no ser única)
    nombre VARCHAR(100),
    genero VARCHAR(10),
    edad INTEGER,
    direccion VARCHAR(100),
    telefono VARCHAR(20),
    contrasena VARCHAR(100),
    estado VARCHAR(20)
);
