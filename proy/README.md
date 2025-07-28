# 🏦 Proyecto de Microservicios Bancarios

Este proyecto implementa una arquitectura de microservicios para un sistema bancario, incluyendo gestión de clientes, cuentas, movimientos y reportes financieros. El sistema está diseñado para ser desplegado tanto localmente con Docker como en la nube con Azure Kubernetes Service (AKS).

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY (8083)                       │
│                    Punto de Entrada Único                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  EUREKA     │ │MICROCLIENTES│ │MICROCUENTAS │
│  SERVER     │ │   (8080)    │ │   (8081)    │
│  (8761)     │ │             │ │             │
└─────────────┘ └─────────────┘ └─────────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ POSTGRESQL  │ │ POSTGRESQL  │ │ POSTGRESQL  │
│  EUREKA     │ │ CLIENTES    │ │  CUENTAS    │
└─────────────┘ └─────────────┘ └─────────────┘
```

## 📋 Componentes del Sistema

### 🔍 **Eureka Server (Puerto 8761)**
- Service Discovery para los microservicios
- Registro y descubrimiento automático de servicios
- Dashboard de monitoreo en `http://localhost:8761`

### 🚪 **API Gateway (Puerto 8083)**
- Punto de entrada único para todas las APIs
- Enrutamiento inteligente a microservicios
- Filtros de logging y monitoreo
- Balanceo de carga automático

### 👥 **Microservicio: microclientes (Puerto 8080)**
Gestiona la información de personas y clientes del banco.

### 💰 **Microservicio: microcuentas (Puerto 8081)**
Maneja la gestión de cuentas bancarias, movimientos y reportes financieros.

## 🚀 Opciones de Despliegue

### 1. 🐳 Despliegue Local con Docker (Recomendado para Desarrollo)

#### Prerrequisitos
- Docker Desktop instalado y ejecutándose
- Docker Compose incluido

#### Despliegue Rápido
```bash
# Navegar al directorio del proyecto
cd Proyectro_Java-main/proy-main/proy

# Desplegar todo con un comando
docker-compose up --build -d

# Verificar el estado
docker-compose ps
```

#### URLs de Acceso
- **Eureka Server:** http://localhost:8761
- **API Gateway:** http://localhost:8083
- **Microclientes:** http://localhost:8080
- **Microcuentas:** http://localhost:8081

### 2. ☁️ Despliegue en Azure AKS (Recomendado para Producción)

#### Prerrequisitos
- Azure CLI instalado
- Terraform instalado
- Cuenta de Azure con permisos de Contributor

#### Despliegue Automático
```bash
# Navegar al directorio de Terraform
cd Proyectro_Java-main/proy-main/proy/terraform

# En Windows
.\deploy.ps1

# En Linux/Mac
chmod +x deploy.sh
./deploy.sh
```

#### Costos Estimados
- **AKS Cluster (2 nodos):** ~$146/mes
- **PostgreSQL (2 instancias):** ~$50/mes
- **Load Balancer:** ~$18/mes
- **Total:** ~$219/mes

## 🔧 Configuración de Bases de Datos

### Base de Datos para microclientes
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

### Base de Datos para microcuentas
```sql
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(20) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    saldo DECIMAL(19,2) NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL,
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta)
);

CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_movimiento_numero_cuenta ON movimiento(numero_cuenta);
```

## 🔄 Flujo Completo a través del API Gateway

Este flujo demuestra cómo usar el sistema completo a través del API Gateway, simulando las solicitudes de Postman.

### **Configuración en Postman**
- **Base URL:** `http://localhost:8083` (API Gateway)
- **Headers:** `Content-Type: application/json`

---

### **Paso 1: Crear una Persona**

**Solicitud:**
```http
POST http://localhost:8083/clientes/personas
Content-Type: application/json

{
  "identificacion": "1001",
  "nombre": "Carlos Gomez",
  "genero": "M",
  "edad": 35,
  "direccion": "Av. Siempre Viva 742",
  "telefono": "555-9876"
}
```

**Respuesta:**
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

---

### **Paso 2: Crear un Cliente**

**Solicitud:**
```http
POST http://localhost:8083/clientes/clientes
Content-Type: application/json

{
  "clienteid": "cli_carlos",
  "contrasena": "clave123",
  "estado": "ACTIVO",
  "identificacion": "1001"
}
```

**Respuesta:**
```json
{
  "clienteid": "cli_carlos",
  "contrasena": "clave123",
  "estado": "ACTIVO",
  "identificacion": "1001",
  "persona": {
    "identificacion": "1001",
    "nombre": "Carlos Gomez",
    "genero": "M",
    "edad": 35,
    "direccion": "Av. Siempre Viva 742",
    "telefono": "555-9876"
  }
}
```

---

### **Paso 3: Crear una Cuenta Bancaria**

**Solicitud:**
```http
POST http://localhost:8083/cuentas/cuentas
Content-Type: application/json

{
  "numeroCuenta": "1001001001",
  "tipoCuenta": "AHORROS",
  "saldoInicial": 500.00,
  "estado": "ACTIVA",
  "clienteId": "cli_carlos"
}
```

**Respuesta:**
```json
{
  "numeroCuenta": "1001001001",
  "tipoCuenta": "AHORROS",
  "saldoInicial": 500.00,
  "estado": "ACTIVA",
  "fechaCreacion": "2024-05-20T10:00:00",
  "fechaActualizacion": "2024-05-20T10:00:00",
  "cliente": {
    "clienteid": "cli_carlos",
    "estado": "ACTIVO",
    "persona": {
      "identificacion": "1001",
      "nombre": "Carlos Gomez",
      "genero": "M",
      "edad": 35,
      "direccion": "Av. Siempre Viva 742",
      "telefono": "555-9876"
    }
  }
}
```

---

### **Paso 4: Realizar un Depósito**

**Solicitud:**
```http
POST http://localhost:8083/cuentas/movimientos
Content-Type: application/json

{
  "tipoMovimiento": "DEPOSITO",
  "valor": 100.00,
  "numeroCuenta": "1001001001",
  "fecha": "2024-05-20T10:30:00"
}
```

**Respuesta:**
```json
{
  "id": 1,
  "fecha": "2024-05-20T10:30:00",
  "tipoMovimiento": "DEPOSITO",
  "valor": 100.00,
  "saldo": 600.00,
  "numeroCuenta": "1001001001",
  "cliente": {
    "clienteid": "cli_carlos",
    "estado": "ACTIVO",
    "persona": {
      "identificacion": "1001",
      "nombre": "Carlos Gomez",
      "genero": "M",
      "edad": 35,
      "direccion": "Av. Siempre Viva 742",
      "telefono": "555-9876"
    }
  }
}
```

---

### **Paso 5: Realizar un Retiro**

**Solicitud:**
```http
POST http://localhost:8083/cuentas/movimientos
Content-Type: application/json

{
  "tipoMovimiento": "RETIRO",
  "valor": 50.00,
  "numeroCuenta": "1001001001",
  "fecha": "2024-05-20T11:00:00"
}
```

**Respuesta:**
```json
{
  "id": 2,
  "fecha": "2024-05-20T11:00:00",
  "tipoMovimiento": "RETIRO",
  "valor": 50.00,
  "saldo": 550.00,
  "numeroCuenta": "1001001001",
  "cliente": {
    "clienteid": "cli_carlos",
    "estado": "ACTIVO",
    "persona": {
      "identificacion": "1001",
      "nombre": "Carlos Gomez",
      "genero": "M",
      "edad": 35,
      "direccion": "Av. Siempre Viva 742",
      "telefono": "555-9876"
    }
  }
}
```

---

### **Paso 6: Obtener Estado de Cuenta**

**Solicitud:**
```http
GET http://localhost:8083/cuentas/reportes/estado-cuenta/1001001001
```

**Respuesta:**
```json
{
  "cuenta": {
    "numeroCuenta": "1001001001",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 500.00,
    "estado": "ACTIVA",
    "fechaCreacion": "2024-05-20T10:00:00",
    "fechaActualizacion": "2024-05-20T11:00:00"
  },
  "cliente": {
    "clienteid": "cli_carlos",
    "estado": "ACTIVO",
    "persona": {
      "identificacion": "1001",
      "nombre": "Carlos Gomez",
      "genero": "M",
      "edad": 35,
      "direccion": "Av. Siempre Viva 742",
      "telefono": "555-9876"
    }
  },
  "movimientos": [
    {
      "id": 1,
      "fecha": "2024-05-20T10:30:00",
      "tipoMovimiento": "DEPOSITO",
      "valor": 100.00,
      "saldo": 600.00
    },
    {
      "id": 2,
      "fecha": "2024-05-20T11:00:00",
      "tipoMovimiento": "RETIRO",
      "valor": 50.00,
      "saldo": 550.00
    }
  ],
  "saldoActual": 550.00
}
```

---

### **Paso 7: Obtener Reporte de Movimientos por Fecha**

**Solicitud:**
```http
GET http://localhost:8083/cuentas/reportes/movimientos?fechaInicio=2024-05-20T00:00:00&fechaFin=2024-05-20T23:59:59
```

**Respuesta:**
```json
{
  "fechaInicio": "2024-05-20T00:00:00",
  "fechaFin": "2024-05-20T23:59:59",
  "totalMovimientos": 2,
  "movimientos": [
    {
      "id": 1,
      "fecha": "2024-05-20T10:30:00",
      "tipoMovimiento": "DEPOSITO",
      "valor": 100.00,
      "saldo": 600.00,
      "numeroCuenta": "1001001001",
      "cliente": {
        "clienteid": "cli_carlos",
        "estado": "ACTIVO",
        "persona": {
          "identificacion": "1001",
          "nombre": "Carlos Gomez",
          "genero": "M",
          "edad": 35,
          "direccion": "Av. Siempre Viva 742",
          "telefono": "555-9876"
        }
      }
    },
    {
      "id": 2,
      "fecha": "2024-05-20T11:00:00",
      "tipoMovimiento": "RETIRO",
      "valor": 50.00,
      "saldo": 550.00,
      "numeroCuenta": "1001001001",
      "cliente": {
        "clienteid": "cli_carlos",
        "estado": "ACTIVO",
        "persona": {
          "identificacion": "1001",
          "nombre": "Carlos Gomez",
          "genero": "M",
          "edad": 35,
          "direccion": "Av. Siempre Viva 742",
          "telefono": "555-9876"
        }
      }
    }
  ]
}
```

---

## 📊 Endpoints del API Gateway

### **Gestión de Personas**
- `GET /clientes/personas` - Listar todas las personas
- `GET /clientes/personas/{identificacion}` - Obtener persona por identificación
- `POST /clientes/personas` - Crear nueva persona
- `PUT /clientes/personas/{identificacion}` - Actualizar persona
- `DELETE /clientes/personas/{identificacion}` - Eliminar persona

### **Gestión de Clientes**
- `GET /clientes/clientes` - Listar todos los clientes
- `GET /clientes/clientes/{clienteid}` - Obtener cliente por ID
- `POST /clientes/clientes` - Crear nuevo cliente
- `PUT /clientes/clientes/{clienteid}` - Actualizar cliente
- `DELETE /clientes/clientes/{clienteid}` - Eliminar cliente

### **Gestión de Cuentas**
- `GET /cuentas/cuentas` - Listar todas las cuentas
- `GET /cuentas/cuentas/{numeroCuenta}` - Obtener cuenta por número
- `POST /cuentas/cuentas` - Crear nueva cuenta
- `PUT /cuentas/cuentas/{numeroCuenta}` - Actualizar cuenta
- `DELETE /cuentas/cuentas/{numeroCuenta}` - Eliminar cuenta

### **Gestión de Movimientos**
- `GET /cuentas/movimientos` - Listar todos los movimientos
- `GET /cuentas/movimientos/{id}` - Obtener movimiento por ID
- `POST /cuentas/movimientos` - Registrar nuevo movimiento
- `GET /cuentas/movimientos/cuenta/{numeroCuenta}` - Listar movimientos por cuenta

### **Reportes**
- `GET /cuentas/reportes/estado-cuenta/{numeroCuenta}` - Estado de cuenta
- `GET /cuentas/reportes/movimientos` - Reporte de movimientos por fecha

## 🔧 Comandos de Gestión

### **Docker (Desarrollo Local)**
```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f microclientes

# Detener todos los servicios
docker-compose down

# Reconstruir y reiniciar
docker-compose up --build -d

# Ver estado de los contenedores
docker-compose ps
```

### **Terraform (Producción Azure)**
```bash
# Navegar al directorio de Terraform
cd terraform

# Inicializar Terraform
terraform init

# Planificar el despliegue
terraform plan

# Aplicar el despliegue
terraform apply

# Ver outputs
terraform output

# Destruir infraestructura
terraform destroy
```

### **Kubernetes (Después del despliegue en AKS)**
```bash
# Configurar kubectl
az aks get-credentials --resource-group rg-microservicios --name aks-microservicios

# Ver pods
kubectl get pods -n microservicios

# Ver servicios
kubectl get services -n microservicios

# Ver logs
kubectl logs -f deployment/eureka-server -n microservicios

# Escalar deployment
kubectl scale deployment microclientes --replicas=3 -n microservicios

# Port forwarding
kubectl port-forward svc/gateway 8083:8083 -n microservicios
```

## 🔒 Seguridad y Validaciones

### **Validaciones de Entrada**
- **Personas:** Identificación única, campos obligatorios
- **Clientes:** ClienteID único, persona asociada debe existir
- **Cuentas:** Número de cuenta único, saldo inicial no negativo
- **Movimientos:** Cuenta debe existir y estar activa, saldo suficiente para retiros

### **Manejo de Errores**
```json
{
  "error": "SaldoInsuficienteException",
  "mensaje": "Saldo insuficiente para realizar el retiro",
  "timestamp": "2024-05-20T11:00:00",
  "path": "/cuentas/movimientos"
}
```

### **Tipos de Error Comunes**
- `SaldoInsuficienteException` - Para retiros sin fondos suficientes
- `IllegalArgumentException` - Para datos de entrada inválidos
- `ResourceNotFoundException` - Para recursos no encontrados
- `ValidationException` - Para errores de validación

## 📈 Monitoreo y Observabilidad

### **Eureka Dashboard**
- URL: http://localhost:8761
- Muestra todos los servicios registrados
- Estado de salud de cada servicio
- Información de instancias

### **Health Checks**
- **Eureka Server:** http://localhost:8761/actuator/health
- **API Gateway:** http://localhost:8083/actuator/health
- **Microclientes:** http://localhost:8080/actuator/health
- **Microcuentas:** http://localhost:8081/actuator/health

### **Métricas de Actuator**
- `/actuator/metrics` - Métricas del sistema
- `/actuator/info` - Información de la aplicación
- `/actuator/env` - Variables de entorno

## 🚀 Próximos Pasos y Mejoras

### **Mejoras Técnicas**
1. **Service Mesh (Istio)** para comunicación entre servicios
2. **Circuit Breaker (Hystrix/Resilience4j)** para tolerancia a fallos
3. **Distributed Tracing (Jaeger/Zipkin)** para seguimiento de requests
4. **Centralized Logging (ELK Stack)** para logs centralizados
5. **API Documentation (Swagger/OpenAPI)** para documentación automática

### **Funcionalidades de Negocio**
1. **Autenticación y Autorización** con JWT/OAuth2
2. **Notificaciones** por email/SMS para movimientos
3. **Reportes Avanzados** con gráficos y estadísticas
4. **Integración con Sistemas Externos** (bancos, proveedores)
5. **Auditoría Completa** de todas las operaciones

### **DevOps y CI/CD**
1. **Pipeline de Integración Continua** con GitHub Actions
2. **Despliegue Automático** a múltiples ambientes
3. **Testing Automatizado** (unit, integration, e2e)
4. **Monitoreo en Tiempo Real** con Prometheus/Grafana
5. **Backup Automático** de bases de datos

## 📞 Soporte y Contacto

### **Recursos Útiles**
- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Documentación de Spring Cloud](https://spring.io/projects/spring-cloud)
- [Documentación de Docker](https://docs.docker.com/)
- [Documentación de Terraform](https://www.terraform.io/docs)
- [Documentación de Azure AKS](https://docs.microsoft.com/en-us/azure/aks/)

### **Solución de Problemas**
1. Verificar que todos los servicios estén ejecutándose
2. Revisar logs con `docker-compose logs` o `kubectl logs`
3. Verificar conectividad de bases de datos
4. Comprobar configuración de Eureka Server
5. Validar endpoints del API Gateway

---

**¡Disfruta usando el sistema de microservicios bancarios! 🎉**

