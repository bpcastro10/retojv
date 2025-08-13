# 🚀 FLUJO COMPLETO DE CUENTAS CON VALIDACIÓN DE CLIENTES

## ✅ **FUNCIONALIDAD IMPLEMENTADA**

El microservicio `microcuentas` ahora implementa **validación completa del cliente** usando **WebFlux** antes de crear cuentas, y proporciona reportes completos con toda la información del cliente.

---

## 🔄 **FLUJO DE CREACIÓN DE CUENTA**

### **1. 📋 Validación Automática del Cliente**

Cuando se crea una cuenta, el sistema automáticamente:

1. **Valida que el cliente existe** usando WebFlux → `microclientes`
2. **Verifica que el cliente está ACTIVO**
3. **Solo entonces permite crear la cuenta**

```java
// CuentaService.crearCuenta()
@Transactional
public Cuenta crearCuenta(Cuenta cuenta) {
    // 1. Validar cliente existe y está ACTIVO (WebFlux)
    validarClienteExisteYActivo(cuenta.getClienteId());
    
    // 2. Crear cuenta solo si validación exitosa
    return cuentaRepository.save(cuenta);
}
```

---

## 🌐 **COMUNICACIÓN WEBFLUX**

### **Arquitectura de Comunicación:**

```
POST /cuentas (microcuentas)
     ↓
CuentaService.crearCuenta()
     ↓
ClienteClient.obtenerClientePorIdentificacion() ← WebFlux
     ↓
WebClient → GET /clientes/identificacion/{id} (microclientes)
     ↓
Validación: ¿Cliente existe? ¿Estado = ACTIVO?
     ↓
Si OK → Crear cuenta
Si ERROR → Excepción con mensaje claro
```

---

## 📊 **EJEMPLOS DE USO COMPLETOS**

### **🔧 1. CREAR CUENTA (Con Validación de Cliente)**

#### **Request:**
```http
POST http://localhost:8081/cuentas
Content-Type: application/json

{
    "numeroCuenta": "1234567890",
    "tipoCuenta": "AHORRO",
    "saldoInicial": 1000.00,
    "estado": "ACTIVA",
    "clienteId": "12345678"
}
```

#### **Proceso Interno:**
1. ✅ WebFlux → Busca cliente "12345678" en microclientes
2. ✅ Valida que existe y estado = "ACTIVO"
3. ✅ Si OK → Crea cuenta
4. ❌ Si cliente no existe/inactivo → Error claro

#### **Response Exitosa:**
```json
{
    "numeroCuenta": "1234567890",
    "tipoCuenta": "AHORRO",
    "saldoInicial": 1000.00,
    "estado": "ACTIVA",
    "clienteId": "12345678",
    "fechaCreacion": "2024-01-15T10:30:00",
    "fechaActualizacion": "2024-01-15T10:30:00"
}
```

#### **Response Error (Cliente Inactivo):**
```json
{
    "error": "No se puede crear cuenta. Cliente 12345678 no existe o no está activo: Cliente no está activo. Estado actual: INACTIVO"
}
```

---

### **💰 2. CREAR MOVIMIENTO (Con Lógica DÉBITO/CRÉDITO)**

#### **Request - Débito:**
```http
POST http://localhost:8081/movimientos
Content-Type: application/json

{
    "numeroCuenta": "1234567890",
    "tipoMovimiento": "DEBITO",
    "valor": 150.00
}
```

#### **Proceso Interno:**
1. ✅ Normaliza valor: 150.00 → -150.00 (automático para DÉBITO)
2. ✅ Calcula nuevo saldo: 1000.00 + (-150.00) = 850.00
3. ✅ Valida saldo suficiente
4. ✅ Actualiza cuenta y crea movimiento

#### **Response:**
```json
{
    "id": 1,
    "fecha": "2024-01-15T14:30:00",
    "tipoMovimiento": "DEBITO",
    "valor": -150.00,
    "saldo": 850.00,
    "cuenta": {
        "numeroCuenta": "1234567890"
    }
}
```

---

### **📈 3. REPORTES COMPLETOS CON INFORMACIÓN DEL CLIENTE**

#### **A. Reporte por Rango de Fechas (Especificación Exacta):**

```http
GET http://localhost:8081/reportes?fecha=2024-01-01T00:00:00,2024-12-31T23:59:59
```

#### **Response:**
```json
[
    {
        "cuenta": {
            "numeroCuenta": "1234567890",
            "tipoCuenta": "AHORRO",
            "saldoInicial": 850.00,
            "estado": "ACTIVA",
            "clienteId": "12345678"
        },
        "cliente": {
            "clienteid": "CLI001",
            "identificacion": "12345678",
            "nombre": "Juan Pérez García",
            "genero": "M",
            "edad": 30,
            "direccion": "Calle 123 #45-67",
            "telefono": "555-1234",
            "contrasena": "******",
            "estado": "ACTIVO"
        },
        "movimientos": [
            {
                "id": 1,
                "fecha": "2024-01-15T14:30:00",
                "tipoMovimiento": "DEBITO",
                "valor": -150.00,
                "saldo": 850.00
            }
        ]
    }
]
```

---

#### **B. Estado de Cuenta Específica:**

```http
GET http://localhost:8081/reportes/estado-cuenta/1234567890
```

#### **Response:** (Mismo formato que arriba, pero para una cuenta específica)

---

#### **C. Reporte Completo por Cliente:**

```http
GET http://localhost:8081/reportes/cliente/12345678/cuentas
```

#### **Response:** (Todas las cuentas del cliente con sus movimientos)

---

### **🔍 4. CONSULTAR CLIENTES (Proxy via WebFlux)**

#### **Por Cliente ID:**
```http
GET http://localhost:8081/clientes/CLI001
```

#### **Por Identificación:**
```http
GET http://localhost:8081/clientes/identificacion/12345678
```

---

## ⚡ **CASOS DE ERROR MANEJADOS**

### **1. Cliente No Existe:**
```json
{
    "error": "No se puede crear cuenta. Cliente 99999999 no existe o no está activo: Cliente no encontrado: 99999999"
}
```

### **2. Cliente Inactivo:**
```json
{
    "error": "No se puede crear cuenta. Cliente 12345678 no existe o no está activo: Cliente no está activo. Estado actual: SUSPENDIDO"
}
```

### **3. Saldo Insuficiente:**
```json
{
    "error": "Saldo no disponible"
}
```

### **4. Microservicio Microclientes No Disponible:**
```json
{
    "error": "No se puede crear cuenta. Cliente 12345678 no existe o no está activo: Connection refused"
}
```

---

## 🎯 **ENDPOINTS COMPLETOS DISPONIBLES**

### **✅ Cuentas:**
```http
POST   /cuentas                      # Crear (con validación cliente)
GET    /cuentas                      # Listar todas
GET    /cuentas/{numero}             # Obtener específica
PUT    /cuentas/{numero}             # Actualizar
DELETE /cuentas/{numero}             # Eliminar
```

### **✅ Movimientos:**
```http
POST   /movimientos                  # Crear (lógica DÉBITO/CRÉDITO automática)
GET    /movimientos                  # Listar todos
GET    /movimientos/{id}             # Obtener específico
GET    /movimientos/cuenta/{numero}  # Por cuenta
```

### **✅ Clientes (Proxy WebFlux):**
```http
GET    /clientes/{clienteid}                     # Por ID
GET    /clientes/identificacion/{identificacion} # Por identificación
```

### **✅ Reportes Completos:**
```http
GET    /reportes?fecha=inicio,fin                    # Por rango (EXACTO)
GET    /reportes/estado-cuenta/{numeroCuenta}        # Por cuenta
GET    /reportes/cliente/{identificacion}/cuentas   # Todas cuentas del cliente
GET    /reportes/movimientos?fechaInicio=...&fechaFin=...  # Solo movimientos
```

---

## 🔄 **FLUJO COMPLETO DE NEGOCIO**

### **Escenario Real:**

1. **Cliente se registra** → `microclientes` (puerto 8080)
2. **Cliente solicita cuenta** → `microcuentas` (puerto 8081)
   - ✅ Validación automática vía WebFlux
   - ✅ Solo se crea si cliente ACTIVO
3. **Cliente hace movimientos** → `microcuentas`
   - ✅ DÉBITO/CRÉDITO automático
   - ✅ Validación de saldo
4. **Cliente solicita estado de cuenta** → `microcuentas`
   - ✅ Información completa del cliente (WebFlux)
   - ✅ Todas las cuentas y movimientos
   - ✅ Formato JSON completo

---

## 🎉 **RESUMEN DE FUNCIONALIDADES**

| Funcionalidad | Estado | Implementación |
|---------------|--------|---------------|
| **Validación Cliente Activo** | ✅ | WebFlux al crear cuenta |
| **Comunicación Asíncrona** | ✅ | WebClient con .block() |
| **CRUD Cuentas Completo** | ✅ | Con validación cliente |
| **Lógica DÉBITO/CRÉDITO** | ✅ | Valores automáticos |
| **Reportes con Cliente** | ✅ | Información completa |
| **Manejo de Errores** | ✅ | Mensajes claros |
| **Estado de Cuenta** | ✅ | JSON completo |

## 🚀 **EL SISTEMA ESTÁ COMPLETAMENTE FUNCIONAL**

✅ **Crear cuenta** → Valida cliente activo vía WebFlux  
✅ **Todos los datos** → Reportes incluyen información completa del cliente  
✅ **Estado de cuenta** → Formato JSON con todo lo necesario  
✅ **Funcionamiento** → Integración completa entre microservicios  

**El microservicio microcuentas ahora cumple 100% con todos los requisitos solicitados.** 🎉