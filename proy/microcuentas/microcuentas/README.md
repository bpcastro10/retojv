# Microservicio de Cuentas

Este microservicio maneja la gestión de cuentas bancarias, movimientos y reportes financieros.

## Requisitos Previos
- Microservicio de Clientes corriendo en el puerto 8082

## Configuración del Entorno

1. Configurar la base de datos PostgreSQL:
   ```sql
   -- Crear la base de datos
   CREATE DATABASE microcuentas;
   
   -- Conectar a la base de datos
   \c microcuentas;
   
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
   
   -- Crear índices para mejorar el rendimiento
   CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
   CREATE INDEX idx_movimiento_numero_cuenta ON movimiento(numero_cuenta);
   
   -- Crear vista para reportes
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
   
   -- Insertar datos de prueba
   INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado)
   VALUES 
       ('1234567890', 'AHORROS', 1000.00, 'ACTIVA'),
       ('0987654321', 'CORRIENTE', 2000.00, 'ACTIVA')
   ON CONFLICT (numero_cuenta) DO NOTHING;
   ```

2. Configurar las credenciales en `application.properties`:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=admin123
   ```


## Endpoints Disponibles

Base URL: `http://localhost:8081`

### Cuentas (`/cuentas`)

#### Listar todas las cuentas
```bash
GET http://localhost:8081/cuentas
```
Respuesta:
```json
[
    {
        "numeroCuenta": "1234567890",
        "tipoCuenta": "AHORROS",
        "saldoInicial": 1000.00,
        "estado": "ACTIVA",
        "fechaCreacion": "2024-03-20T10:00:00",
        "fechaActualizacion": "2024-03-20T10:00:00"
    },
    {
        "numeroCuenta": "0987654321",
        "tipoCuenta": "CORRIENTE",
        "saldoInicial": 2000.00,
        "estado": "ACTIVA",
        "fechaCreacion": "2024-03-20T10:00:00",
        "fechaActualizacion": "2024-03-20T10:00:00"
    }
]
```

#### Obtener cuenta específica
```bash
GET http://localhost:8081/cuentas/1234567890
```
Respuesta:
```json
{
    "numeroCuenta": "1234567890",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 1000.00,
    "estado": "ACTIVA",
    "fechaCreacion": "2024-03-20T10:00:00",
    "fechaActualizacion": "2024-03-20T10:00:00"
}
```

#### Crear nueva cuenta
```bash
POST http://localhost:8081/cuentas
Content-Type: application/json

{
    "numeroCuenta": "9876543210",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA"
}
```
Respuesta:
```json
{
    "numeroCuenta": "9876543210",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA",
    "fechaCreacion": "2024-03-20T10:00:00",
    "fechaActualizacion": "2024-03-20T10:00:00"
}
```

#### Actualizar cuenta
```bash
PUT http://localhost:8081/cuentas/1234567890
Content-Type: application/json

{
    "tipoCuenta": "CORRIENTE",
    "saldoInicial": 1500.00,
    "estado": "ACTIVA"
}
```
Respuesta:
```json
{
    "numeroCuenta": "1234567890",
    "tipoCuenta": "CORRIENTE",
    "saldoInicial": 1500.00,
    "estado": "ACTIVA",
    "fechaCreacion": "2024-03-20T10:00:00",
    "fechaActualizacion": "2024-03-20T10:00:00"
}
```

#### Eliminar cuenta
```bash
DELETE http://localhost:8081/cuentas/1234567890
```
Respuesta:
```json
{
    "mensaje": "Cuenta eliminada exitosamente"
}
```

### Movimientos (`/movimientos`)

#### Listar todos los movimientos
```bash
GET http://localhost:8081/movimientos
```
Respuesta:
```json
[
    {
        "id": 1,
        "fecha": "2024-03-20T10:00:00",
        "tipoMovimiento": "DEPOSITO",
        "valor": 500.00,
        "saldo": 1500.00,
        "numeroCuenta": "1234567890",
        "cliente": {
            "id": 1,
            "nombre": "Juan Pérez",
            "identificacion": "1234567890"
        }
    },
    {
        "id": 2,
        "fecha": "2024-03-20T11:00:00",
        "tipoMovimiento": "RETIRO",
        "valor": 200.00,
        "saldo": 1300.00,
        "numeroCuenta": "1234567890",
        "cliente": {
            "id": 1,
            "nombre": "Juan Pérez",
            "identificacion": "1234567890"
        }
    }
]
```

#### Obtener movimiento específico
```bash
GET http://localhost:8081/movimientos/1
```
Respuesta:
```json
{
    "id": 1,
    "fecha": "2024-03-20T10:00:00",
    "tipoMovimiento": "DEPOSITO",
    "valor": 500.00,
    "saldo": 1500.00,
    "numeroCuenta": "1234567890",
    "cliente": {
        "id": 1,
        "nombre": "Juan Pérez",
        "identificacion": "1234567890"
    }
}
```

#### Registrar nuevo movimiento
```bash
POST http://localhost:8081/movimientos
Content-Type: application/json

{
    "tipoMovimiento": "DEPOSITO",
    "valor": 500.00,
    "numeroCuenta": "1234567890",
    "fecha": "2024-03-20T10:00:00"
}
```
Respuesta:
```json
{
    "id": 3,
    "fecha": "2024-03-20T12:00:00",
    "tipoMovimiento": "DEPOSITO",
    "valor": 500.00,
    "saldo": 1800.00,
    "numeroCuenta": "1234567890",
    "cliente": {
        "id": 1,
        "nombre": "Juan Pérez",
        "identificacion": "1234567890",
        "direccion": "Calle Principal 123",
        "telefono": "555-0123",
        "estado": true
    }
}
```

#### Actualizar movimiento
```bash
PUT http://localhost:8081/movimientos/1
Content-Type: application/json

{
    "tipoMovimiento": "DEPOSITO",
    "valor": 1000.00,
    "numeroCuenta": "1234567890"
}
```
Respuesta:
```json
{
    "id": 1,
    "fecha": "2024-03-20T10:00:00",
    "tipoMovimiento": "DEPOSITO",
    "valor": 1000.00,
    "saldo": 2000.00,
    "numeroCuenta": "1234567890",
    "cliente": {
        "id": 1,
        "nombre": "Juan Pérez",
        "identificacion": "1234567890"
    }
}
```

#### Eliminar movimiento
```bash
DELETE http://localhost:8081/movimientos/1
```
Respuesta:
```json
{
    "mensaje": "Movimiento eliminado exitosamente"
}
```

### Reportes (`/reportes`)

#### Estado de cuenta
```bash
GET http://localhost:8081/reportes/estado-cuenta/1234567890
```
Respuesta:
```json
{
    "cuenta": {
        "numeroCuenta": "1234567890",
        "tipoCuenta": "AHORROS",
        "saldoInicial": 1000.00,
        "estado": "ACTIVA",
        "fechaCreacion": "2024-03-20T10:00:00",
        "fechaActualizacion": "2024-03-20T10:00:00"
    },
    "cliente": {
        "id": 1,
        "nombre": "Juan Pérez",
        "identificacion": "1234567890",
        "direccion": "Calle Principal 123",
        "telefono": "555-0123",
        "estado": true
    },
    "movimientos": [
        {
            "id": 1,
            "fecha": "2024-03-20T10:00:00",
            "tipoMovimiento": "DEPOSITO",
            "valor": 500.00,
            "saldo": 1500.00
        },
        {
            "id": 2,
            "fecha": "2024-03-20T11:00:00",
            "tipoMovimiento": "RETIRO",
            "valor": 200.00,
            "saldo": 1300.00
        }
    ]
}
```

#### Reporte de movimientos por fecha
```bash
GET http://localhost:8081/reportes/movimientos?fechaInicio=2024-03-01T00:00:00&fechaFin=2024-03-31T23:59:59
```
Respuesta:
```json
[
    {
        "id": 1,
        "fecha": "2024-03-20T10:00:00",
        "tipoMovimiento": "DEPOSITO",
        "valor": 500.00,
        "saldo": 1500.00,
        "numeroCuenta": "1234567890",
        "cliente": {
            "id": 1,
            "nombre": "Juan Pérez",
            "identificacion": "1234567890",
            "direccion": "Calle Principal 123",
            "telefono": "555-0123",
            "estado": true
        }
    },
    {
        "id": 2,
        "fecha": "2024-03-20T11:00:00",
        "tipoMovimiento": "RETIRO",
        "valor": 200.00,
        "saldo": 1300.00,
        "numeroCuenta": "1234567890",
        "cliente": {
            "id": 1,
            "nombre": "Juan Pérez",
            "identificacion": "1234567890",
            "direccion": "Calle Principal 123",
            "telefono": "555-0123",
            "estado": true
        }
    }
]
```

### Respuestas de Error

#### Error de Validación
```json
{
    "error": "Error de Validación",
    "mensaje": "El saldo inicial no puede ser negativo"
}
```

#### Error de Saldo Insuficiente
```json
{
    "error": "Saldo Insuficiente",
    "mensaje": "No hay fondos suficientes para realizar la operación"
}
```

#### Error de Recurso No Encontrado
```json
{
    "error": "Recurso No Encontrado",
    "mensaje": "La cuenta con número 1234567890 no existe"
}
```

#### Error del Sistema
```json
{
    "error": "Error del Sistema",
    "mensaje": "Ha ocurrido un error inesperado"
}
```

## Integración con Microservicio de Clientes

El microservicio se integra con el microservicio de clientes mediante Feign Client:

- `ClienteClient`: Interface para comunicación con el microservicio de clientes
- Endpoints disponibles:
  - `GET http://localhost:8082/clientes/{id}`: Obtiene información de un cliente por ID
  - `GET http://localhost:8082/clientes/cuenta/{numeroCuenta}`: Obtiene información de un cliente por número de cuenta

