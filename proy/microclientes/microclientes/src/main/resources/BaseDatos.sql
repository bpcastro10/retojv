-- Script SQL para crear la nueva estructura de base de datos
-- Microservicio Microclientes con herencia Persona -> Cliente

-- ==========================================
-- ELIMINAR TABLAS EXISTENTES (si existen)
-- ==========================================
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS persona CASCADE;
DROP TABLE IF EXISTS persona_cliente CASCADE;

-- ==========================================
-- CREAR TABLA PERSONA (Entidad Base)
-- ==========================================
CREATE TABLE persona (
    identificacion VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(1) NOT NULL,
    edad INTEGER NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    
    -- Constraints
    CONSTRAINT pk_persona PRIMARY KEY (identificacion),
    CONSTRAINT chk_persona_genero CHECK (genero IN ('M', 'F')),
    CONSTRAINT chk_persona_edad CHECK (edad >= 0 AND edad <= 150),
    CONSTRAINT chk_persona_identificacion CHECK (identificacion ~ '^[0-9]{8,20}$'),
    CONSTRAINT chk_persona_nombre CHECK (LENGTH(nombre) >= 2 AND LENGTH(nombre) <= 100),
    CONSTRAINT chk_persona_direccion CHECK (LENGTH(direccion) >= 5 AND LENGTH(direccion) <= 100),
    CONSTRAINT chk_persona_telefono CHECK (LENGTH(telefono) >= 7 AND LENGTH(telefono) <= 20)
);

-- ==========================================
-- CREAR TABLA CLIENTE (Hereda de Persona)
-- ==========================================
CREATE TABLE cliente (
    identificacion VARCHAR(20) NOT NULL,
    clienteid VARCHAR(20) NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    
    -- Constraints
    CONSTRAINT pk_cliente PRIMARY KEY (identificacion),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (identificacion) REFERENCES persona(identificacion) ON DELETE CASCADE,
    CONSTRAINT uk_cliente_clienteid UNIQUE (clienteid),
    CONSTRAINT chk_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
    CONSTRAINT chk_cliente_clienteid CHECK (clienteid ~ '^[A-Z0-9]{3,20}$'),
    CONSTRAINT chk_cliente_contrasena CHECK (LENGTH(contrasena) >= 6 AND LENGTH(contrasena) <= 100)
);

-- ==========================================
-- CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ==========================================
CREATE INDEX idx_persona_nombre ON persona(nombre);
CREATE INDEX idx_persona_genero ON persona(genero);
CREATE INDEX idx_persona_edad ON persona(edad);

CREATE INDEX idx_cliente_estado ON cliente(estado);
CREATE INDEX idx_cliente_identificacion ON cliente(identificacion);

-- ==========================================
-- DATOS DE EJEMPLO
-- ==========================================
-- Insertar personas de ejemplo
INSERT INTO persona (identificacion, nombre, genero, edad, direccion, telefono) VALUES
('12345678', 'Juan Pérez García', 'M', 30, 'Calle 123 #45-67', '555-1234'),
('87654321', 'María López Rodríguez', 'F', 25, 'Carrera 89 #12-34', '555-5678'),
('11223344', 'Carlos Mendoza Silva', 'M', 35, 'Avenida 56 #78-90', '555-9012');

-- Insertar clientes de ejemplo
INSERT INTO cliente (identificacion, clienteid, contrasena, estado) VALUES
('12345678', 'CLI001', 'password123', 'ACTIVO'),
('87654321', 'CLI002', 'password456', 'ACTIVO'),
('11223344', 'CLI003', 'password789', 'INACTIVO');