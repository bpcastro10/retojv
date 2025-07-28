# Proyecto de Microservicios Bancarios

Este proyecto consta de dos microservicios: `microclientes` (gestión de personas y clientes) y `microcuentas` (gestión de cuentas, movimientos y reportes).

## Microservicio: microclientes

Este microservicio gestiona la información de personas y clientes.

### Configuración y Ejecución

1.  **Base de Datos:**
    -   Crea una base de datos en PostgreSQL (por ejemplo, `microclientesdb`).
    -   Ejecuta el script SQL para crear las tablas:
```sql
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
```
    -   Configura la conexión en `microclientes/microclientes/src/main/resources/application.properties`.

    ```
    -   El microservicio de clientes se ejecuta por defecto en el puerto **8080**.

### Endpoints Principales (`http://localhost:8080`)

-   **Personas:**
    -   `POST /personas`: Crear persona
        -   **Método:** `POST`
        -   **URL:** `http://localhost:8080/personas`
        -   **Body:** `raw`, `JSON`

```json
{
  "identificacion": "1001",
  "nombre": "Carlos Gomez",
  "genero": "M",
  "edad": 35,
  "direccion": "Av. Siempre Viva 742",
  "telefono": "555-9876"
}
```
    -   `GET /personas`: Listar todas las personas
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8080/personas`
    -   `GET /personas/{identificacion}`: Obtener persona por identificación
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8080/personas/{identificacion}` (reemplazar {identificacion})
    -   `PUT /personas/{identificacion}`: Actualizar persona
        -   **Método:** `PUT`
        -   **URL:** `http://localhost:8080/personas/{identificacion}` (reemplazar {identificacion})
        -   **Body:** `raw`, `JSON`

```json
{
  "identificacion": "1001",
  "nombre": "Carlos Gomez",
  "genero": "M",
  "edad": 36,
  "direccion": "Av. Siempre Viva 742",
  "telefono": "555-9876"
}
```
    -   `DELETE /personas/{identificacion}`: Eliminar persona
        -   **Método:** `DELETE`
        -   **URL:** `http://localhost:8080/personas/{identificacion}` (reemplazar {identificacion})

-   **Clientes:**
    -   `POST /clientes`: Crear cliente (requiere que la Persona asociada exista)
        -   **Método:** `POST`
        -   **URL:** `http://localhost:8080/clientes`
        -   **Body:** `raw`, `JSON`

```json
{
  "clienteid": "cli_carlos",
  "contrasena": "clave123",
  "estado": "ACTIVO",
  "identificacion": "1001" 
}
```
    -   `GET /clientes`: Listar todos los clientes
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8080/clientes`
    -   `GET /clientes/{clienteid}`: Obtener cliente por ID
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8080/clientes/{clienteid}` (reemplazar {clienteid})
    -   `PUT /clientes/{clienteid}`: Actualizar cliente
        -   **Método:** `PUT`
        -   **URL:** `http://localhost:8080/clientes/{clienteid}` (reemplazar {clienteid})
        -   **Body:** `raw`, `JSON`

```json
{
  "clienteid": "cli_carlos",
  "contrasena": "clave456",
  "estado": "INACTIVO",
  "identificacion": "1001" 
}
```
    -   `DELETE /clientes/{clienteid}`: Eliminar cliente
        -   **Método:** `DELETE`
        -   **URL:** `http://localhost:8080/clientes/{clienteid}` (reemplazar {clienteid})

## Microservicio: microcuentas

Este microservicio maneja la gestión de cuentas bancarias, movimientos y reportes financieros.


### Configuración y Ejecución

1.  **Base de Datos:**
    -   Crea una base de datos en PostgreSQL (por ejemplo, `microcuentasdb`).
    -   Ejecuta el script SQL para crear las tablas, índices y vista (ubicado en `microcuentas/microcuentas/src/main/resources/schema.sql`):
```sql
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
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_numero_cuenta ON movimiento(numero_cuenta);

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
```
    -   Configura la conexión en `microcuentas/microcuentas/src/main/resources/application.properties`.
    -   Asegúrate de que `spring.jpa.hibernate.ddl-auto` esté configurado como `create` o que las tablas existan antes de ejecutar.

2.  **Compilación y Ejecución:**
    ```bash
    cd microcuentas/microcuentas
    mvn clean install
    mvn spring-boot:run
    ```
    -   El microservicio de cuentas se ejecuta por defecto en el puerto **8081**.

### Endpoints Disponibles (`http://localhost:8081`)

-   **Cuentas:**
    -   `GET /cuentas`: Listar todas las cuentas
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/cuentas`
    -   `GET /cuentas/{numeroCuenta}`: Obtener cuenta por número
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/cuentas/{numeroCuenta}` (reemplazar {numeroCuenta})
    -   `POST /cuentas`: Crear nueva cuenta
        -   **Método:** `POST`
        -   **URL:** `http://localhost:8081/cuentas`
        -   **Body:** `raw`, `JSON`

```json
{
    "numeroCuenta": "1001001001",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA",
    "clienteId": "cli_carlos" 
}
```
    -   `PUT /cuentas/{numeroCuenta}`: Actualizar cuenta
        -   **Método:** `PUT`
        -   **URL:** `http://localhost:8081/cuentas/{numeroCuenta}` (reemplazar {numeroCuenta})
        -   **Body:** `raw`, `JSON`

```json
{
    "tipoCuenta": "CORRIENTE",
    "saldoInicial": 600.00,
    "estado": "ACTIVA"
}
```
    -   `DELETE /cuentas/{numeroCuenta}`: Eliminar cuenta
        -   **Método:** `DELETE`
        -   **URL:** `http://localhost:8081/cuentas/{numeroCuenta}` (reemplazar {numeroCuenta})

-   **Movimientos:**
    -   `GET /movimientos`: Listar todos los movimientos
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/movimientos`
    -   `GET /movimientos/{id}`: Obtener movimiento por ID
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/movimientos/{id}` (reemplazar {id})
    -   `POST /movimientos`: Registrar nuevo movimiento
        -   **Método:** `POST`
        -   **URL:** `http://localhost:8081/movimientos`
        -   **Body:** `raw`, `JSON`

```json
{
    "tipoMovimiento": "DEPOSITO",
    "valor": 100.00,
    "numeroCuenta": "1001001001",
    "fecha": "2024-05-20T10:30:00" 
}
```
    -   `GET /movimientos/cuenta/{numeroCuenta}`: Listar movimientos por cuenta
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/movimientos/cuenta/{numeroCuenta}` (reemplazar {numeroCuenta})

-   **Reportes:**
    -   `GET /reportes/estado-cuenta/{numeroCuenta}`: Obtener estado de cuenta
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/reportes/estado-cuenta/{numeroCuenta}` (reemplazar {numeroCuenta})
    -   `GET /reportes/movimientos`: Reporte de movimientos por fecha
        -   **Método:** `GET`
        -   **URL:** `http://localhost:8081/reportes/movimientos`
        -   **Params:** `fechaInicio`, `fechaFin`

## Flujo de Prueba Completo con Bases de Datos Vacías

Este flujo asume que ambos microservicios están compilados pero sus bases de datos están vacías (o se han recreado). Utilizaremos **Postman** para interactuar con los endpoints.

**Nota Importante:** Asegúrate de tener ambos microservicios (`microclientes` en 8080 y `microcuentas` en 8081) corriendo antes de ejecutar este flujo.

### Paso 1: Crear una Persona (en microclientes)

Necesitamos una persona para asociarla a un cliente.

-   **Método:** `POST`
-   **URL:** `http://localhost:8080/personas`
-   **Body:** `raw`, `JSON`

```json
{
  "identificacion": "1001",
  "nombre": "Carlos Gomez",
  "genero": "M",
  "edad": 35,
  "direccion": "Av. Siempre Viva 742",
  "telefono": "555-9876"
}
```

*   **Verificación (Opcional):** Listar personas.
    -   **Método:** `GET`
    -   **URL:** `http://localhost:8080/personas`

### Paso 2: Crear un Cliente (en microclientes)

Creamos un cliente asociado a la persona creada en el Paso 1.

-   **Método:** `POST`
-   **URL:** `http://localhost:8080/clientes`
-   **Body:** `raw`, `JSON`

```json
{
  "clienteid": "cli_carlos",
  "contrasena": "clave123",
  "estado": "ACTIVO",
  "identificacion": "1001" 
}
```
*   **Verificación (Opcional):** Listar clientes.
    -   **Método:** `GET`
    -   **URL:** `http://localhost:8080/clientes`

### Paso 3: Crear una Cuenta (en microcuentas)

Creamos una cuenta bancaria. **Importante:** Esta cuenta debe ser asociada a un cliente existente en el microservicio de clientes. Usaremos el `clienteid` creado en el Paso 2 para esto.

-   **Método:** `POST`
-   **URL:** `http://localhost:8081/cuentas`
-   **Body:** `raw`, `JSON`

```json
{
    "numeroCuenta": "1001001001",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA",
    "clienteId": "cli_carlos" 
}
```
*   **Verificación (Opcional):** Listar cuentas.
    -   **Método:** `GET`
    -   **URL:** `http://localhost:8081/cuentas`

### Paso 4: Registrar un Movimiento (en microcuentas)

Realizamos un depósito en la cuenta creada en el Paso 3.

-   **Método:** `POST`
-   **URL:** `http://localhost:8081/movimientos`
-   **Body:** `raw`, `JSON`

```json
{
    "tipoMovimiento": "DEPOSITO",
    "valor": 100.00,
    "numeroCuenta": "1001001001",
    "fecha": "2024-05-20T10:30:00" 
}
```
*   **Nota:** Puedes ajustar la fecha y hora según sea necesario.

*   **Verificación (Opcional):** Listar movimientos.
    -   **Método:** `GET`
    -   **URL:** `http://localhost:8081/movimientos`

### Paso 5: Obtener Estado de Cuenta (en microcuentas)

Genera un reporte del estado de cuenta, que incluye información de la cuenta, el cliente (obtenido del otro microservicio) y los movimientos.

-   **Método:** `GET`
-   **URL:** `http://localhost:8081/reportes/estado-cuenta/1001001001`

### Paso 6: Obtener Reporte de Movimientos por Fecha (en microcuentas)

Genera un reporte de movimientos en un rango de fechas específico.

-   **Método:** `GET`
-   **URL:** `http://localhost:8081/reportes/movimientos`
-   **Params:**
    -   `fechaInicio`: `2024-01-01T00:00:00`
    -   `fechaFin`: `2024-12-31T23:59:59`
*   **Nota:** Ajusta las fechas según sea necesario en la pestaña 'Params' de Postman.

---

## Formatos de Respuesta

Aquí se describen los formatos JSON de las respuestas exitosas para algunas operaciones clave.

### Cuenta (Ejemplo de GET /cuentas/{numeroCuenta})
```json
{
    "numeroCuenta": "1001001001",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA",
    "fechaCreacion": "2024-05-20T10:00:00",
    "fechaActualizacion": "2024-05-20T10:00:00"
}
```

### Movimiento (Ejemplo de GET /movimientos/{id})
```json
{
    "id": 1,
    "fecha": "2024-05-20T10:30:00",
    "tipoMovimiento": "DEPOSITO",
    "valor": 100.00,
    "saldo": 600.00,
    "numeroCuenta": "1001001001",
    "cliente": {
        "id": "cli_carlos",
        "nombre": "Carlos Gomez",
        "identificacion": "1001",
        "direccion": "Av. Siempre Viva 742",
        "telefono": "555-9876",
        "estado": "ACTIVO"
    }
}
```

### Estado de Cuenta (Ejemplo de GET /reportes/estado-cuenta/{numeroCuenta})
```json
{
    "cuenta": {
        "numeroCuenta": "1001001001",
        "tipoCuenta": "AHORROS",
        "saldoInicial": 500.00,
        "estado": "ACTIVA",
        "fechaCreacion": "2024-05-20T10:00:00",
        "fechaActualizacion": "2024-05-20T10:00:00"
    },
    "cliente": {
        "id": "cli_carlos",
        "nombre": "Carlos Gomez",
        "identificacion": "1001",
        "direccion": "Av. Siempre Viva 742",
        "telefono": "555-9876",
        "estado": "ACTIVO"
    },
    "movimientos": [
        {
            "id": 1,
            "fecha": "2024-05-20T10:30:00",
            "tipoMovimiento": "DEPOSITO",
            "valor": 100.00,
            "saldo": 600.00
        }
    ]
}
```

### Respuesta de Error (Formato General)
```json
{
    "error": "Tipo de Error",
    "mensaje": "Descripción detallada del error"
}
```

---

## Manejo de Errores

El sistema maneja diversas excepciones y devuelve respuestas con formatos de error consistentes.

-   `SaldoInsuficienteException`: Para operaciones que intentan retirar más fondos de los disponibles.
-   `IllegalArgumentException`: Para errores de validación de datos de entrada.
-   Otros errores del sistema: Capturados por el `GlobalExceptionHandler`.

## Validaciones

El microservicio de cuentas realiza validaciones a nivel de aplicación para:

### Cuentas
- Número de cuenta es un campo requerido.
- Tipo de cuenta debe ser 'AHORROS' o 'CORRIENTE'.
- Saldo inicial no puede ser negativo.
- Estado debe ser 'ACTIVA' o 'INACTIVA'.

### Movimientos
- La cuenta asociada debe existir y estar activa.
- El valor del movimiento no puede ser nulo.
- Para retiros, se valida que el saldo resultante no sea negativo (`SaldoInsuficienteException`).
- El tipo de movimiento es requerido.
- La fecha del movimiento es requerida.

## Compilación y Ejecución

1.  Clona el repositorio.
2.  Navega a la raíz del proyecto en tu terminal.
3.  Compila ambos microservicios:
    ```bash
    cd microclientes/microclientes
    mvn clean install
    cd ../../microcuentas/microcuentas
    mvn clean install
    cd ../../..
    ```
4.  Ejecuta el microservicio de `microclientes`:
    ```bash
    cd microclientes/microclientes
    mvn spring-boot:run
    ```
5.  Abre otra terminal, navega a la raíz del proyecto y ejecuta el microservicio de `microcuentas`:
    ```bash
    cd microcuentas/microcuentas
    mvn spring-boot:run
    ```

Ambos microservicios estarán corriendo en `http://localhost:8080` y `http://localhost:8081` respectivamente.

