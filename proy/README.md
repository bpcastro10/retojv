# 🏦 Proyecto de Microservicios Bancarios

Este proyecto implementa una arquitectura de microservicios para un sistema bancario, incluyendo gestión de clientes, cuentas, movimientos y reportes financieros. El sistema está diseñado para ser desplegado tanto localmente con Docker como en la nube con Azure Kubernetes Service (AKS).

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY (8083)                       │
│                    Punto de Entrada Único                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    EUREKA SERVER                            │
│                   (Puerto 8761)                             │
│                                                             │
│ • Service Discovery                                         │
│ • Health Monitoring                                         │
│ • Load Balancing                                            │
│ • Dashboard de monitoreo                                    │
│ • Auto-registration de servicios                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│MICROCLIENTES│ │MICROCUENTAS │ │   GATEWAY   │
│(Puerto 0*)  │ │(Puerto 0*)  │ │(Puerto 8083)│
│             │ │             │ │             │
│• Gestión    │ │• Gestión    │ │• Ruteo      │
│  Personas   │ │  Cuentas    │ │• Filtros    │
│• Gestión    │ │• Movimientos│ │• Load Bal.  │
│  Clientes   │ │• Reportes   │ │• CORS       │
└─────────────┘ └─────────────┘ └─────────────┘
        │             │             |
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼
        │             │             
        ▼             ▼             
┌─────────────┐ ┌─────────────┐ 
│ POSTGRESQL  │ │ POSTGRESQL  │ 
│ CLIENTES    │ │  CUENTAS    │ 
│(Puerto 5432)│ │(Puerto 5433)│ 
└─────────────┘ └─────────────┘ 

* Puerto 0 = Puerto aleatorio para múltiples instancias
```

### **Arquitectura de Despliegue Cloud (Azure AKS)**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                       │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AZURE AKS CLUSTER                                      │
│                                                                             │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐                │
│ │   API GATEWAY   │ │  EUREKA SERVER  │ │  LOAD BALANCER  │                │
│ │   (Puerto 8083) │ │  (Puerto 8761)  │ │   (Azure LB)    │                │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘                │
│                                                                             │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐                │
│ │ MICROCLIENTES   │ │ MICROCUENTAS    │ │   NAMESPACE     │                │
│ │ (2 Replicas)    │ │ (2 Replicas)    │ │ microservicios  │                │
│ │ (Puertos 0*)    │ │ (Puertos 0*)    │ │                 │                │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘                │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ AZURE       │ │ AZURE       │ │ AZURE       │
│ POSTGRESQL  │ │ POSTGRESQL  │ │ CONTAINER   │
│ CLIENTES    │ │ CUENTAS     │ │ REGISTRY    │
└─────────────┘ └─────────────┘ └─────────────┘

* Puerto 0 = Puerto aleatorio asignado por Kubernetes
```

## 📋 Componentes del Sistema

### 🔍 **Eureka Server (Puerto 8761)**
- Service Discovery para los microservicios
- Registro y descubrimiento automático de servicios
- Dashboard de monitoreo en `http://localhost:8761`
- **No requiere base de datos** - usa memoria interna para registro

### 🚪 **API Gateway (Puerto 8083)**
- Punto de entrada único para todas las APIs
- Enrutamiento inteligente a microservicios
- Filtros de logging y monitoreo
- Balanceo de carga automático

### 👥 **Microservicio: microclientes (Puerto 0*)**
Gestiona la información de personas y clientes del banco.
- **Puerto aleatorio** para permitir múltiples instancias
- Se registra automáticamente en Eureka
- Descubierto dinámicamente por el Gateway

### 💰 **Microservicio: microcuentas (Puerto 0*)**
Maneja la gestión de cuentas bancarias, movimientos y reportes financieros.
- **Puerto aleatorio** para permitir múltiples instancias
- Se registra automáticamente en Eureka
- Descubierto dinámicamente por el Gateway

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
- **Microclientes:** http://localhost:8080 (puerto fijo en Docker)
- **Microcuentas:** http://localhost:8081 (puerto fijo en Docker)

#### Comandos Útiles
```bash
# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f microclientes

# Detener todos los servicios
docker-compose down

# Reconstruir y reiniciar
docker-compose up --build -d
```

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

### 3. 🔧 Despliegue Manual (Desarrollo)

#### Orden de Inicio Recomendado

```bash
# 1. Iniciar Eureka Server (Puerto 8761)
cd proy-main/proy/eureka-server
.\mvnw spring-boot:run

# 2. Iniciar Microclientes (Puerto aleatorio)
cd proy-main/proy/microclientes/microclientes
.\mvnw spring-boot:run

# 3. Iniciar Microcuentas (Puerto aleatorio)
cd proy-main/proy/microcuentas/microcuentas
.\mvnw spring-boot:run

# 4. Iniciar Gateway (Puerto 8083)
cd proy-main/proy/gateway/gateway
.\mvnw spring-boot:run
```

#### Scripts Automatizados

#### Para Desarrollo (Puertos Fijos):
```bash
run-dev-mode.bat
```
- Eureka: `http://localhost:8761`
- Microclientes: `http://localhost:8080`
- Microcuentas: `http://localhost:8081`
- Gateway: `http://localhost:8083`

#### Para Producción/Testing (Puertos Aleatorios):
```bash
run-multiple-instances.bat
```
- Cada instancia tendrá puerto aleatorio
- Revisa Eureka Dashboard para ver puertos asignados

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
- Información de instancias y puertos asignados

### **Health Checks**
- **Eureka Server:** http://localhost:8761/actuator/health
- **API Gateway:** http://localhost:8083/actuator/health
- **Microclientes:** http://localhost:8080/actuator/health (o puerto asignado)
- **Microcuentas:** http://localhost:8081/actuator/health (o puerto asignado)

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
6. Verificar puertos asignados en Eureka Dashboard

---

## 🏗️ Patrones de Diseño Implementados

### **Patrones Arquitectónicos Principales**

#### **1. Service Discovery Pattern (Eureka)**
**Propósito**: Registro y descubrimiento automático de servicios
**Implementación**: Netflix Eureka Server
**Ubicación**: `eureka-server/src/main/java/com/proyecto/eureka_server/EurekaServerApplication.java`
**Beneficios**:
- Registro automático de microservicios al iniciar
- Descubrimiento dinámico de instancias disponibles
- Load balancing automático entre múltiples instancias
- Health monitoring en tiempo real
- Auto-cancelación de servicios caídos

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### **2. API Gateway Pattern (Spring Cloud Gateway)**
**Propósito**: Punto de entrada único para todas las APIs
**Implementación**: Spring Cloud Gateway
**Ubicación**: `gateway/gateway/src/main/java/com/proyecto/gateway/GatewayApplication.java`
**Beneficios**:
- Centralización de cross-cutting concerns
- Simplificación del cliente
- Seguridad centralizada
- Monitoreo unificado
- Ruteo inteligente con Service Discovery

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: clientes-service
          uri: lb://microclientes
          predicates:
            - Path=/clientes/**
```

#### **3. Repository Pattern**
**Propósito**: Abstracción del acceso a datos
**Implementación**: Spring Data JPA
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/repository/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/repository/`
**Beneficios**:
- Separación de lógica de negocio y acceso a datos
- Facilita testing con mocks
- Independencia de la tecnología de persistencia

```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByClienteid(String clienteid);
}
```

#### **4. Service Layer Pattern**
**Propósito**: Lógica de negocio centralizada
**Implementación**: @Service
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/service/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/service/`
**Beneficios**:
- Separación de lógica de negocio
- Reutilización de código
- Facilita testing

```java
@Service
public class ClienteService {
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Lógica de negocio
    }
}
```

#### **5. DTO (Data Transfer Object) Pattern**
**Propósito**: Transferencia de datos entre capas
**Implementación**: DTOs específicos para cada operación
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/dto/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/dto/`
**Beneficios**:
- Separación de entidades de dominio y datos de transferencia
- Control de qué datos se exponen
- Versionado de APIs

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String clienteid;
    private String estado;
    private PersonaDTO persona;
}
```

#### **6. MVC (Model-View-Controller) Pattern**
**Propósito**: Separación de responsabilidades en la capa de presentación
**Implementación**: Spring MVC
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/controller/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/controller/`
**Beneficios**:
- Separación clara de responsabilidades
- Facilita testing y mantenimiento
- Reutilización de componentes

```java
@RestController
@RequestMapping("/clientes")
public class ClienteController {
    @PostMapping
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        // Controller: maneja requests HTTP
        // Model: ClienteDTO
        // View: JSON Response
    }
}
```

#### **7. Dependency Injection Pattern**
**Propósito**: Inversión de control y gestión de dependencias
**Implementación**: Spring IoC Container
**Ubicación**: Todo el proyecto (anotaciones @Autowired, @Inject)
**Beneficios**:
- Desacoplamiento de componentes
- Facilita testing con mocks
- Gestión automática del ciclo de vida

```java
@Service
public class ClienteService {
    @Autowired  // Dependency Injection
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ModelMapper modelMapper;
}
```

#### **8. Layered Architecture Pattern**
**Propósito**: Separación de responsabilidades por capas
**Implementación**: Estructura de paquetes organizada
**Ubicación**: Estructura de directorios en cada microservicio
**Beneficios**:
- Separación clara de responsabilidades
- Facilita mantenimiento y testing
- Escalabilidad por capas

```
com.proyecto.microclientes/
├── controller/    # Capa de presentación (MVC)
├── service/       # Capa de negocio (Service Layer)
├── repository/    # Capa de datos (Repository)
├── entity/        # Modelo de dominio (Entity)
├── dto/          # Objetos de transferencia (DTO)
├── config/       # Configuración (Configuration)
└── exception/    # Manejo de errores (Exception Handler)
```

### **Patrones Creacionales**

#### **9. Singleton Pattern**
**Propósito**: Garantizar una sola instancia de una clase
**Implementación**: Spring Beans (por defecto)
**Ubicación**: Todas las clases con anotaciones @Service, @Repository, @Configuration
**Beneficios**:
- Control de instancias
- Gestión de recursos
- Consistencia de estado

```java
@Service  // Singleton por defecto en Spring
public class ClienteService {
    // Una sola instancia por contexto de aplicación
}
```

#### **10. Factory Method Pattern**
**Propósito**: Crear objetos sin especificar su clase exacta
**Implementación**: Spring Framework (Bean Factory)
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/config/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/config/`
**Beneficios**:
- Flexibilidad en la creación de objetos
- Encapsulación de lógica de creación
- Extensibilidad

```java
@Configuration
public class ModelMapperConfig {
    @Bean  // Factory Method para crear ModelMapper
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
```

#### **11. Builder Pattern**
**Propósito**: Construir objetos complejos paso a paso
**Implementación**: Lombok @Builder
**Ubicación**: DTOs y entidades en ambos microservicios
**Beneficios**:
- Construcción flexible de objetos
- Inmutabilidad
- Legibilidad del código

```java
@Data
@Builder  // Patrón Builder automático
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String clienteid;
    private String estado;
    private PersonaDTO persona;
}
```

### **Patrones Estructurales**

#### **12. Adapter Pattern**
**Propósito**: Permitir que interfaces incompatibles trabajen juntas
**Implementación**: ModelMapper
**Ubicación**: Servicios en ambos microservicios
**Beneficios**:
- Integración de sistemas incompatibles
- Reutilización de código existente
- Flexibilidad en la interfaz

```java
@Service
public class ClienteService {
    @Autowired
    private ModelMapper modelMapper;  // Adapter para conversión
    
    public ClienteDTO convertirAEntidad(Cliente cliente) {
        return modelMapper.map(cliente, ClienteDTO.class);  // Adaptación
    }
}
```

#### **13. Facade Pattern**
**Propósito**: Proporcionar una interfaz simplificada a un subsistema complejo
**Implementación**: API Gateway
**Ubicación**: `gateway/gateway/src/main/java/com/proyecto/gateway/`
**Beneficios**:
- Simplificación de la interfaz del cliente
- Reducción de acoplamiento
- Encapsulación de complejidad

```java
// El Gateway actúa como Facade para todos los microservicios
// Simplifica la interfaz del cliente ocultando la complejidad interna
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    // Facade para todos los microservicios
}
```

#### **14. Proxy Pattern**
**Propósito**: Proporcionar un sustituto o marcador de posición para otro objeto
**Implementación**: OpenFeign
**Ubicación**: `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/client/`
**Beneficios**:
- Control de acceso a objetos
- Caching y optimización
- Logging y monitoreo

```java
@FeignClient(name = "microclientes")  // Proxy para comunicación HTTP
public interface ClienteClient {
    @GetMapping("/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable("id") String id);
}
```

### **Patrones de Comportamiento**

#### **15. Template Method Pattern**
**Propósito**: Definir el esqueleto de un algoritmo en una clase base
**Implementación**: Spring Boot Application
**Ubicación**: Clases principales de cada microservicio
**Beneficios**:
- Reutilización de código
- Extensibilidad
- Control del flujo de ejecución

```java
@SpringBootApplication
public class MicroclientesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroclientesApplication.class, args);  // Template Method
    }
}
```

#### **16. Strategy Pattern**
**Propósito**: Definir una familia de algoritmos y hacerlos intercambiables
**Implementación**: Diferentes estrategias de validación
**Ubicación**: 
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/service/`
**Beneficios**:
- Flexibilidad en algoritmos
- Eliminación de condicionales complejos
- Extensibilidad

```java
@Service
public class MovimientoService {
    public void validarMovimiento(MovimientoDTO movimiento) {
        // Diferentes estrategias según el tipo de movimiento
        if ("RETIRO".equals(movimiento.getTipoMovimiento())) {
            validarRetiro(movimiento);
        } else if ("DEPOSITO".equals(movimiento.getTipoMovimiento())) {
            validarDeposito(movimiento);
        }
    }
}
```

#### **17. Observer Pattern**
**Propósito**: Definir una dependencia uno-a-muchos entre objetos
**Implementación**: Spring Events (preparado)
**Ubicación**: Preparado para implementar en ambos microservicios
**Beneficios**:
- Desacoplamiento entre componentes
- Notificaciones automáticas
- Extensibilidad

```java
// Preparado para implementar eventos
@Component
public class ApplicationEventListener {
    @EventListener
    public void handleClienteCreado(ClienteCreadoEvent event) {
        // Observador de eventos
    }
}
```

#### **18. Exception Handler Pattern**
**Propósito**: Manejo centralizado de excepciones
**Implementación**: @ControllerAdvice
**Ubicación**: 
- `microclientes/microclientes/src/main/java/com/proyecto/microclientes/exception/`
- `microcuentas/microcuentas/src/main/java/com/proyecto/microcuentas/exception/`
**Beneficios**:
- Respuestas de error consistentes
- Logging centralizado
- Separación de lógica de manejo de errores

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("SaldoInsuficienteException", e.getMessage()));
    }
}
```

### **Patrones de Comunicación**

#### **19. Client-Server Pattern**
**Propósito**: Separación de responsabilidades entre cliente y servidor
**Implementación**: Arquitectura REST
**Ubicación**: Comunicación entre microservicios
**Beneficios**:
- Separación de responsabilidades
- Escalabilidad independiente
- Mantenimiento simplificado

```java
// Cliente (microcuentas) → Servidor (microclientes)
@FeignClient(name = "microclientes")
public interface ClienteClient {
    @GetMapping("/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable("id") String id);
}
```

#### **20. Multiple Instance Pattern**
**Propósito**: Ejecutar múltiples instancias del mismo servicio
**Implementación**: Puertos aleatorios (`server.port=0`)
**Ubicación**: `application.properties` de cada microservicio
**Beneficios**:
- Alta disponibilidad
- Load balancing automático
- Escalabilidad horizontal
- Zero-downtime deployments

```properties
# Configuración para múltiples instancias
server.port=0
eureka.instance.instance-id=${spring.application.name}:${random.uuid}
```

## 📊 Resumen de Patrones Implementados

| Categoría | Patrón | Implementación | Ubicación | Estado |
|-----------|--------|----------------|-----------|---------|
| **Arquitectónicos** | Service Discovery | Eureka Server | `eureka-server/` | ✅ |
| | API Gateway | Spring Cloud Gateway | `gateway/gateway/` | ✅ |
| | Repository | Spring Data JPA | `repository/` | ✅ |
| | Service Layer | @Service | `service/` | ✅ |
| | DTO | DTOs específicos | `dto/` | ✅ |
| | MVC | Spring MVC | `controller/` | ✅ |
| | Dependency Injection | Spring IoC | Todo el proyecto | ✅ |
| | Layered Architecture | Estructura de paquetes | Estructura de directorios | ✅ |
| **Creacionales** | Singleton | Spring Beans | @Service, @Repository | ✅ |
| | Factory Method | Spring Bean Factory | @Configuration | ✅ |
| | Builder | Lombok @Builder | DTOs y entidades | ✅ |
| **Estructurales** | Adapter | ModelMapper | Servicios | ✅ |
| | Facade | API Gateway | `gateway/` | ✅ |
| | Proxy | OpenFeign | `client/` | ✅ |
| **Comportamiento** | Template Method | Spring Boot | Clases principales | ✅ |
| | Strategy | Validaciones | Servicios | ✅ |
| | Observer | Spring Events | Preparado | ✅ |
| | Exception Handler | @ControllerAdvice | `exception/` | ✅ |
| **Comunicación** | Client-Server | REST APIs | Comunicación entre servicios | ✅ |
| | Multiple Instance | Puertos aleatorios | `application.properties` | ✅ |

## 🎯 Beneficios de los Patrones Implementados

### **Arquitectura Sólida**
- **Separación de responsabilidades** clara
- **Escalabilidad** horizontal y vertical
- **Mantenibilidad** del código
- **Testabilidad** de componentes

### **Resiliencia**
- **Service Discovery** para alta disponibilidad
- **Load Balancing** automático
- **Health Checks** para monitoreo
- **Manejo de errores** centralizado

### **Flexibilidad**
- **Múltiples instancias** por servicio
- **Comunicación declarativa** entre servicios
- **Configuración centralizada**
- **Despliegue independiente**

---

**¡Disfruta usando el sistema de microservicios bancarios! 🎉**
