# Microservicio: Microclientes

Este microservicio gestiona la información de personas y clientes para el sistema bancario, implementando herencia JPA con Persona como entidad base y Cliente como entidad derivada.

---

## 🏗️ Arquitectura de Herencia

### Entidad Base: Persona
- **PK:** `identificacion` (String)
- **Campos:** nombre, género, edad, dirección, teléfono

### Entidad Derivada: Cliente
- **Hereda de:** Persona
- **PK:** `identificacion` (heredada)
- **Campos adicionales:** clienteid (único), contraseña, estado

---

## 📊 Estructura de Base de Datos

```sql
-- Tabla Persona (Base)
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(1) NOT NULL,
    edad INTEGER NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL
);

-- Tabla Cliente (Herencia)
CREATE TABLE cliente (
    identificacion VARCHAR(20) PRIMARY KEY,
    clienteid VARCHAR(20) UNIQUE NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion) ON DELETE CASCADE
);
```

---

## 🚀 Instrucciones de Ejecución

### 1. Configurar Base de Datos
```bash
# Crear base de datos PostgreSQL
createdb microclientesdb
```

### 2. Configurar application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/microclientesdb
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
```

### 3. Ejecutar Microservicio
```bash
# Compilar y ejecutar
./mvnw clean compile
./mvnw spring-boot:run

# El microservicio corre en puerto 8080
```

---

## 📋 Endpoints Disponibles

### ✅ Verbos HTTP Implementados
- **GET** - Consultar recursos
- **POST** - Crear recursos
- **PUT** - Actualizar completo
- **PATCH** - Actualizar parcial ⭐ **NUEVO**
- **DELETE** - Eliminar recursos

---

## 👥 Endpoints de Clientes

### 1. 📝 Crear Cliente (POST)
```http
POST http://localhost:8080/clientes
Content-Type: application/json

{
  "identificacion": "12345678",
  "nombre": "Juan Pérez García",
  "genero": "M",
  "edad": 30,
  "direccion": "Calle 123 #45-67",
  "telefono": "555-1234",
  "clienteid": "CLI001",
  "contrasena": "MyPass123!",
  "estado": "ACTIVO"
}
```

**Pasos en Postman:**
1. Método: `POST`
2. URL: `http://localhost:8080/clientes`
3. Headers: `Content-Type: application/json`
4. Body → raw → JSON
5. Pegar el JSON de arriba
6. Click **Send**

**Respuesta Exitosa (201):**
```json
{
  "identificacion": "12345678",
  "nombre": "Juan Pérez García",
  "genero": "M",
  "edad": 30,
  "direccion": "Calle 123 #45-67",
  "telefono": "555-1234",
  "clienteid": "CLI001",
  "contrasena": "MyPass123!",
  "estado": "ACTIVO"
}
```

### 2. 📋 Listar Todos los Clientes (GET)
```http
GET http://localhost:8080/clientes
```

**Pasos en Postman:**
1. Método: `GET`
2. URL: `http://localhost:8080/clientes`
3. Click **Send**

**Respuesta Exitosa (200):**
```json
[
  {
    "identificacion": "12345678",
    "nombre": "Juan Pérez García",
    "genero": "M",
    "edad": 30,
    "direccion": "Calle 123 #45-67",
    "telefono": "555-1234",
    "clienteid": "CLI001",
    "contrasena": "MyPass123!",
    "estado": "ACTIVO"
  }
]
```

### 3. 🔍 Buscar Cliente por ID (GET)
```http
GET http://localhost:8080/clientes/CLI001
```

**Pasos en Postman:**
1. Método: `GET`
2. URL: `http://localhost:8080/clientes/CLI001`
3. Click **Send**

### 4. 🔍 Buscar Cliente por Identificación (GET)
```http
GET http://localhost:8080/clientes/identificacion/12345678
```

**Pasos en Postman:**
1. Método: `GET`
2. URL: `http://localhost:8080/clientes/identificacion/12345678`
3. Click **Send**

### 5. ✏️ Actualizar Cliente Completo (PUT)
```http
PUT http://localhost:8080/clientes/CLI001
Content-Type: application/json

{
  "identificacion": "12345678",
  "nombre": "Juan Carlos Pérez García",
  "genero": "M",
  "edad": 31,
  "direccion": "Avenida 456 #78-90",
  "telefono": "555-9999",
  "clienteid": "CLI001",
  "contrasena": "NewPass456!",
  "estado": "ACTIVO"
}
```

**Pasos en Postman:**
1. Método: `PUT`
2. URL: `http://localhost:8080/clientes/CLI001`
3. Headers: `Content-Type: application/json`
4. Body → raw → JSON
5. Pegar el JSON completo
6. Click **Send**

### 6. 🔧 Actualizar Cliente Parcial (PATCH) ⭐
```http
PATCH http://localhost:8080/clientes/CLI001
Content-Type: application/json

{
  "telefono": "555-7777",
  "estado": "INACTIVO"
}
```

**Pasos en Postman:**
1. Método: `PATCH`
2. URL: `http://localhost:8080/clientes/CLI001`
3. Headers: `Content-Type: application/json`
4. Body → raw → JSON
5. Solo incluir campos a actualizar
6. Click **Send**

**Ejemplos de PATCH:**

**Cambiar solo el estado:**
```json
{
  "estado": "SUSPENDIDO"
}
```

**Actualizar datos personales:**
```json
{
  "nombre": "Juan Carlos Pérez",
  "edad": 32,
  "direccion": "Nueva Dirección 123"
}
```

**Cambiar contraseña:**
```json
{
  "contrasena": "SuperSecure789!"
}
```

### 7. 🗑️ Eliminar Cliente (DELETE)
```http
DELETE http://localhost:8080/clientes/CLI001
```

**Pasos en Postman:**
1. Método: `DELETE`
2. URL: `http://localhost:8080/clientes/CLI001`
3. Click **Send**

**Respuesta Exitosa (204):** No Content

---

## 📋 Ejemplos de JSON Válidos

### Cliente Completo (POST/PUT)
```json
{
  "identificacion": "87654321",
  "nombre": "María López Rodríguez",
  "genero": "F",
  "edad": 25,
  "direccion": "Carrera 89 #12-34",
  "telefono": "555-5678",
  "clienteid": "CLI002",
  "contrasena": "SecurePass123!",
  "estado": "ACTIVO"
}
```

### Actualización Parcial (PATCH)
```json
{
  "edad": 26,
  "telefono": "555-0000"
}
```

---

## ⚠️ Validaciones Implementadas

### Persona (Campos Base)
- **identificacion:** 8-20 dígitos numéricos, PK única
- **nombre:** 2-100 caracteres, solo letras y espacios
- **genero:** 'M' o 'F'
- **edad:** 0-150 años
- **direccion:** 5-100 caracteres
- **telefono:** 7-20 caracteres, números y símbolos

### Cliente (Campos Específicos)
- **clienteid:** 3-20 caracteres alfanuméricos mayúsculas, único
- **contrasena:** 6-100 caracteres, requiere mayúscula, minúscula, número y símbolo especial
- **estado:** 'ACTIVO', 'INACTIVO', 'SUSPENDIDO'

---

## 🔍 Códigos de Respuesta HTTP

| Código | Descripción | Cuándo |
|--------|-------------|---------|
| **200** | OK | GET, PUT, PATCH exitosos |
| **201** | Created | POST exitoso |
| **204** | No Content | DELETE exitoso |
| **400** | Bad Request | Datos inválidos |
| **404** | Not Found | Cliente no encontrado |
| **500** | Internal Server Error | Error del servidor |

---

## 🧪 Casos de Prueba Recomendados

### 1. Flujo Completo de CRUD
```bash
# 1. Crear cliente
POST /clientes → 201

# 2. Listar clientes
GET /clientes → 200

# 3. Buscar específico
GET /clientes/CLI001 → 200

# 4. Actualizar parcial
PATCH /clientes/CLI001 → 200

# 5. Actualizar completo
PUT /clientes/CLI001 → 200

# 6. Eliminar
DELETE /clientes/CLI001 → 204
```

### 2. Casos de Error
```bash
# Cliente no encontrado
GET /clientes/NOEXISTE → 404

# Datos inválidos
POST /clientes con JSON inválido → 400

# Identificación duplicada
POST /clientes con identificación existente → 400
```

### 3. Validaciones
```bash
# Género inválido
"genero": "X" → 400

# Edad fuera de rango
"edad": 200 → 400

# Contraseña débil
"contrasena": "123" → 400
```

---

## 🔗 Colección de Postman

### Importar Colección
Crear una nueva colección en Postman con estos endpoints:

1. **Microclientes CRUD**
   - Crear Cliente (POST)
   - Listar Clientes (GET)
   - Buscar por ID (GET)
   - Buscar por Identificación (GET)
   - Actualizar Completo (PUT)
   - Actualizar Parcial (PATCH)
   - Eliminar Cliente (DELETE)

### Variables de Entorno
```json
{
  "baseUrl": "http://localhost:8080",
  "clienteId": "CLI001",
  "identificacion": "12345678"
}
```

---

## 🏆 Características Implementadas

✅ **Herencia JPA** (Persona → Cliente)  
✅ **5 Verbos HTTP** (GET, POST, PUT, PATCH, DELETE)  
✅ **Validaciones Bean Validation**  
✅ **Manejo de Errores Global**  
✅ **Actualización Parcial con Reflexión**  
✅ **Claves Primarias Correctas**  
✅ **Documentación Completa**  

---

**¡El microservicio está completamente funcional y cumple con todos los requisitos especificados!** 🎉