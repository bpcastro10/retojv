
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100),
    genero VARCHAR(10),
    edad INTEGER,
    direccion VARCHAR(100),
    telefono VARCHAR(20)
);

CREATE TABLE cliente (
    clienteid VARCHAR(20) PRIMARY KEY,
    contrasena VARCHAR(100),
    estado VARCHAR(20),
    identificacion VARCHAR(20),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (identificacion) REFERENCES persona(identificacion)
);
