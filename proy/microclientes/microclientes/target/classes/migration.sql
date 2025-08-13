-- Script de migración para implementar herencia Persona -> Cliente
-- Este script migra los datos de la tabla persona_cliente a las nuevas tablas persona y cliente

-- Crear tabla persona (entidad base)
CREATE TABLE IF NOT EXISTS persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(1) NOT NULL CHECK (genero IN ('M', 'F')),
    edad INTEGER NOT NULL CHECK (edad >= 0 AND edad <= 150),
    direccion VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL
);

-- Crear tabla cliente (hereda de persona)
CREATE TABLE IF NOT EXISTS cliente (
    clienteid VARCHAR(20) PRIMARY KEY,
    identificacion VARCHAR(20) NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion) ON DELETE CASCADE
);

-- Migrar datos existentes de persona_cliente a las nuevas tablas
-- Paso 1: Migrar datos a la tabla persona
INSERT INTO persona (identificacion, nombre, genero, edad, direccion, telefono)
SELECT DISTINCT identificacion, nombre, genero, edad, direccion, telefono
FROM persona_cliente
WHERE identificacion NOT IN (SELECT identificacion FROM persona)
ON CONFLICT (identificacion) DO NOTHING;

-- Paso 2: Migrar datos a la tabla cliente
INSERT INTO cliente (clienteid, identificacion, contrasena, estado)
SELECT clienteid, identificacion, contrasena, estado
FROM persona_cliente
WHERE clienteid NOT IN (SELECT clienteid FROM cliente)
ON CONFLICT (clienteid) DO NOTHING;

-- Verificación de datos migrados
SELECT 
    'Personas migradas' as tabla, 
    COUNT(*) as total 
FROM persona
UNION ALL
SELECT 
    'Clientes migrados' as tabla, 
    COUNT(*) as total 
FROM cliente
UNION ALL
SELECT 
    'Registros originales' as tabla, 
    COUNT(*) as total 
FROM persona_cliente;

-- Opcional: Comentar estas líneas después de verificar que la migración fue exitosa
-- DROP TABLE IF EXISTS persona_cliente;

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_cliente_identificacion ON cliente(identificacion);
CREATE INDEX IF NOT EXISTS idx_persona_nombre ON persona(nombre);
CREATE INDEX IF NOT EXISTS idx_cliente_estado ON cliente(estado);

-- Comentarios sobre la migración
-- 1. La tabla persona_cliente original se mantiene hasta verificar que todo funciona correctamente
-- 2. Se usa ON CONFLICT DO NOTHING para evitar errores en caso de re-ejecutar el script
-- 3. La relación entre persona y cliente es 1:1 a través de identificacion
-- 4. Se mantienen todas las validaciones de constraints originales