
-- Crear tabla de cuentas
CREATE TABLE IF NOT EXISTS cuenta (
    numero_cuenta VARCHAR(20) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de movimientos
CREATE TABLE IF NOT EXISTS movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    saldo DECIMAL(19,2) NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL,
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta)
);

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_numero_cuenta ON movimiento(numero_cuenta);

-- Crear vista
CREATE OR REPLACE VIEW vista_estado_cuenta AS
SELECT 
    c.numero_cuenta,
    c.tipo_cuenta,
    c.saldo_inicial,
    c.estado,
    m.fecha,
    m.tipo_movimiento,
    m.valor,
    m.saldo
FROM cuenta c
LEFT JOIN movimiento m ON c.numero_cuenta = m.numero_cuenta;
