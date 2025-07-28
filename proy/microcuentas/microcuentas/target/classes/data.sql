-- Insertar datos de prueba en la tabla cuenta
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado)
VALUES 
    ('1234567890', 'AHORROS', 1000.00, 'ACTIVA'),
    ('0987654321', 'CORRIENTE', 2000.00, 'ACTIVA')
ON CONFLICT (numero_cuenta) DO NOTHING;

-- Insertar datos de prueba en la tabla movimiento
INSERT INTO movimiento (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES 
    (CURRENT_TIMESTAMP, 'DEPOSITO', 500.00, 1500.00, '1234567890'),
    (CURRENT_TIMESTAMP, 'RETIRO', 200.00, 1300.00, '1234567890')
ON CONFLICT DO NOTHING; 