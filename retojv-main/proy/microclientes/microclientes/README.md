# Microservicio: microclientes

Este microservicio gestiona la información de personas y clientes para el sistema bancario.

---

## Importante: Crear primero la Persona

Antes de crear un cliente, **debes crear primero la persona** asociada, ya que el cliente referencia a una persona existente en la base de datos. Si intentas crear un cliente con una persona que no existe, obtendrás un error como:

```
Unable to find com.proyecto.microclientes.entity.Persona with id 87654321
```

### 1. Crear una persona (POST)
- **Método:** POST
- **URL:** `http://localhost:8080/personas`
- **Body (raw, JSON):**
```json
{
  "identificacion": "87654321",
  "nombre": "Maria Lopez",
  "genero": "F",
  "edad": 28,
  "direccion": "Calle 2",
  "telefono": "555-5678"
}
```
- **Pasos en Postman:**
  1. Selecciona método `POST`.
  2. Escribe la URL.
  3. Ve a la pestaña **Body** > selecciona **raw** > elige **JSON**.
  4. Pega el JSON de arriba.
  5. Haz clic en **Send**.

### 2. Listar todas las personas (GET)
- **Método:** GET
- **URL:** `http://localhost:8080/personas`
- **Pasos en Postman:**
  1. Selecciona método `GET`.
  2. Escribe la URL.
  3. Haz clic en **Send**.

### 3. Obtener una persona por ID (GET)
- **Método:** GET
- **URL:** `http://localhost:8080/personas/87654321`
- **Pasos en Postman:**
  1. Selecciona método `GET`.
  2. Escribe la URL con la identificación deseada.
  3. Haz clic en **Send**.

### 4. Actualizar una persona (PUT)
- **Método:** PUT
- **URL:** `http://localhost:8080/personas/87654321`
- **Body (raw, JSON):**
```json
{
  "identificacion": "87654321",
  "nombre": "Maria Lopez",
  "genero": "F",
  "edad": 29,
  "direccion": "Calle 2",
  "telefono": "555-5678"
}
```
- **Pasos en Postman:**
  1. Selecciona método `PUT`.
  2. Escribe la URL con la identificación a actualizar.
  3. Ve a la pestaña **Body** > selecciona **raw** > elige **JSON**.
  4. Pega el JSON de arriba.
  5. Haz clic en **Send**.

### 5. Eliminar una persona (DELETE)
- **Método:** DELETE
- **URL:** `http://localhost:8080/personas/87654321`
- **Pasos en Postman:**
  1. Selecciona método `DELETE`.
  2. Escribe la URL con la identificación a eliminar.
  3. Haz clic en **Send**.

---

## Instrucciones de despliegue y ejecución

1. **Configura la base de datos:**
   - Crea una base de datos en PostgreSQL, por ejemplo: `microclientesdb`.
   - Modifica el archivo `src/main/resources/application.properties` con tus credenciales y URL de la base de datos.

2. **Crea las tablas ejecutando este SQL:**
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
- La columna `identificacion` en `cliente` es una clave foránea que referencia a la persona correspondiente.

4. **Compila y ejecuta el microservicio:**
```bash
mvn clean install
mvn spring-boot:run
```
   - Por defecto, el microservicio corre en el puerto 8080.

---

## Estructura de entidades y relación (JPA)

- **Persona**: entidad base (abstracta), identificacion es la PK.
- **Cliente**: hereda de Persona, clienteid es la PK, y tiene una relación ManyToOne con Persona a través de la clave foránea `identificacion`.

Ejemplo de relación en la entidad Cliente:
```java
@ManyToOne
@JoinColumn(name = "identificacion", referencedColumnName = "identificacion")
private Persona persona;
```

---

## Endpoints principales

### 1. Listar todos los clientes
- **GET** `/clientes`
- **Respuesta:**
```json
[
  {
    "clienteid": "cli001",
    "contrasena": "1234",
    "estado": "ACTIVO",
    "identificacion": "12345678",
    "nombre": "Juan Perez",
    "genero": "M",
    "edad": 30,
    "direccion": "Calle 1",
    "telefono": "555-1234"
  }
]
```

### 2. Obtener un cliente por ID
- **GET** `/clientes/{clienteid}`
- **Ejemplo:** `/clientes/cli001`

### 3. Crear un cliente
- **POST** `/clientes`
- **Body:**
```json
{
  "clienteid": "cli002",
  "contrasena": "abcd",
  "estado": "ACTIVO",
  "persona": {
    "identificacion": "87654321"
  }
}
```
- **Respuesta:** Cliente creado

### 4. Actualizar un cliente
- **PUT** `/clientes/{clienteid}`
- **Body:** (igual que POST)

### 5. Eliminar un cliente
- **DELETE** `/clientes/{clienteid}`

---

## Cómo probar con Postman

### 2. Crear un cliente (POST)
- **Método:** POST
- **URL:** `http://localhost:8080/clientes`
- **Body (raw, JSON):**
```json
{
  "clienteid": "cli002",
  "contrasena": "abcd",
  "estado": "ACTIVO",
  "persona": {
    "identificacion": "87654321"
  }
}
```
- **Nota:** Solo necesitas enviar el identificador de la persona ya creada.

### 3. Listar todos los clientes (GET)
- **Método:** GET
- **URL:** `http://localhost:8080/clientes`
- **Pasos en Postman:**
  1. Selecciona método `GET`.
  2. Escribe la URL.
  3. Haz clic en **Send**.

### 4. Obtener un cliente por ID (GET)
- **Método:** GET
- **URL:** `http://localhost:8080/clientes/cli002`
- **Pasos en Postman:**
  1. Selecciona método `GET`.
  2. Escribe la URL con el `clienteid` deseado.
  3. Haz clic en **Send**.

### 5. Actualizar un cliente (PUT)
- **Método:** PUT
- **URL:** `http://localhost:8080/clientes/cli002`
- **Body (raw, JSON):**
```json
{
  "clienteid": "cli002",
  "contrasena": "nueva",
  "estado": "INACTIVO",
  "persona": {
    "identificacion": "87654321",
    "nombre": "Maria Lopez",
    "genero": "F",
    "edad": 29,
    "direccion": "Calle 2",
    "telefono": "555-5678"
  }
}
```
- **Pasos en Postman:**
  1. Selecciona método `PUT`.
  2. Escribe la URL con el `clienteid` a actualizar.
  3. Ve a la pestaña **Body** > selecciona **raw** > elige **JSON**.
  4. Pega el JSON de arriba.
  5. Haz clic en **Send**.

### 6. Eliminar un cliente (DELETE)
- **Método:** DELETE
- **URL:** `http://localhost:8080/clientes/cli002`
- **Pasos en Postman:**
  1. Selecciona método `DELETE`.
  2. Escribe la URL con el `clienteid` a eliminar.
  3. Haz clic en **Send**.

---

## Notas
- Cambia el puerto si tu aplicación usa otro.
- Asegúrate de tener la base de datos configurada y corriendo.
- Puedes importar las peticiones a una colección en Postman para mayor comodidad.

---

## Ejemplo de respuesta exitosa (GET /clientes/cli002)
```json
{
  "clienteid": "cli002",
  "contrasena": "nueva",
  "estado": "INACTIVO",
  "persona": {
    "identificacion": "87654321",
    "nombre": "Maria Lopez",
    "genero": "F",
    "edad": 29,
    "direccion": "Calle 2",
    "telefono": "555-5678"
  }
}
``` 