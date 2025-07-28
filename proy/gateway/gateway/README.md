# API Gateway - Spring Cloud Gateway

Este es el API Gateway centralizado para todos los microservicios del proyecto. Proporciona un punto de entrada único para todas las APIs.

## 🚀 Características

- **Ruteo centralizado**: Todas las peticiones pasan por el gateway
- **CORS configurado**: Permite peticiones desde cualquier origen
- **Logging detallado**: Registra todas las peticiones y respuestas
- **Monitoreo**: Endpoints de health check y estado de rutas
- **Filtros personalizados**: Headers adicionales y logging

## 📋 Configuración

### Puerto
- **Gateway**: 8083
- **Microclientes**: 8080
- **Microcuentas**: 8081

### Rutas Configuradas

| Servicio | Ruta Gateway | Servicio Destino | URI Destino |
|----------|--------------|------------------|-------------|
| Clientes | `/api/v1/clientes/**` | Microclientes | `http://localhost:8080` |
| Cuentas | `/api/v1/cuentas/**` | Microcuentas | `http://localhost:8081` |
| Movimientos | `/api/v1/movimientos/**` | Microcuentas | `http://localhost:8081` |
| Reportes | `/api/v1/reportes/**` | Microcuentas | `http://localhost:8081` |

## 🛠️ Instalación y Ejecución

### Prerrequisitos
- Java 17+
- Maven 3.6+
- Microservicios ejecutándose en sus puertos correspondientes

### Ejecutar el Gateway

```bash
cd proy-main/proy/gateway/gateway
./mvnw spring-boot:run
```

### Verificar que esté funcionando

```bash
curl http://localhost:8083/gateway/health
```

## 📡 Endpoints del Gateway

### Información del Gateway
- `GET /gateway/health` - Estado del gateway
- `GET /gateway/routes` - Lista de rutas configuradas

### Endpoints de Microservicios (a través del gateway)

#### Clientes
```bash
# Obtener todos los clientes
GET http://localhost:8082/api/v1/clientes

# Obtener cliente por ID
GET http://localhost:8082/api/v1/clientes/{id}

# Crear cliente
POST http://localhost:8082/api/v1/clientes

# Actualizar cliente
PUT http://localhost:8082/api/v1/clientes/{id}

# Eliminar cliente
DELETE http://localhost:8082/api/v1/clientes/{id}
```

#### Cuentas
```bash
# Obtener todas las cuentas
GET http://localhost:8082/api/v1/cuentas

# Obtener cuenta por ID
GET http://localhost:8082/api/v1/cuentas/{id}

# Crear cuenta
POST http://localhost:8082/api/v1/cuentas

# Actualizar cuenta
PUT http://localhost:8082/api/v1/cuentas/{id}

# Eliminar cuenta
DELETE http://localhost:8082/api/v1/cuentas/{id}
```

#### Movimientos
```bash
# Obtener todos los movimientos
GET http://localhost:8082/api/v1/movimientos

# Obtener movimientos por cuenta
GET http://localhost:8082/api/v1/movimientos/cuenta/{numeroCuenta}

# Crear movimiento
POST http://localhost:8082/api/v1/movimientos

# Reporte por fecha
GET http://localhost:8082/api/v1/movimientos/reporte?fechaInicio=...&fechaFin=...
```

#### Reportes
```bash
# Estado de cuenta
GET http://localhost:8082/api/v1/reportes/estado-cuenta/{numeroCuenta}

# Reporte de movimientos
GET http://localhost:8082/api/v1/reportes/movimientos?fechaInicio=...&fechaFin=...
```

## 🔧 Configuración Avanzada

### Logging
El gateway registra automáticamente:
- Todas las peticiones entrantes
- Headers de request
- Status de respuesta
- Timestamp de cada operación

### CORS
Configurado para permitir:
- Todos los orígenes (`*`)
- Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Todos los headers
- Credenciales habilitadas

### Timeouts
- **Connect Timeout**: 5 segundos
- **Response Timeout**: 10 segundos

## 📊 Monitoreo

### Actuator Endpoints
- `/actuator/health` - Estado de salud
- `/actuator/gateway/routes` - Rutas del gateway
- `/actuator/metrics` - Métricas del sistema

### Headers Personalizados
El gateway agrega automáticamente:
- `X-Response-Time`: Timestamp de la petición
- `X-Gateway-Source`: Identificador del gateway

## 🚨 Troubleshooting

### Problemas Comunes

1. **Gateway no puede conectarse a microservicios**
   - Verificar que los microservicios estén ejecutándose
   - Confirmar puertos correctos en configuración

2. **Errores de CORS**
   - Verificar configuración de CORS en gateway
   - Revisar headers en peticiones del cliente

3. **Timeouts**
   - Ajustar timeouts en `application.properties`
   - Verificar rendimiento de microservicios

### Logs
Los logs detallados están habilitados en nivel DEBUG para:
- `org.springframework.cloud.gateway`
- `reactor.netty`

## 🔄 Flujo de Peticiones

```
Cliente → Gateway (8082) → Microservicio (8080/8081) → Base de Datos
```

1. Cliente hace petición al gateway
2. Gateway valida ruta y aplica filtros
3. Gateway reenvía petición al microservicio correspondiente
4. Microservicio procesa y responde
5. Gateway devuelve respuesta al cliente

## 📝 Notas Importantes

- El gateway **NO** afecta el funcionamiento de los microservicios
- Los microservicios siguen siendo accesibles directamente en sus puertos
- El gateway solo proporciona un punto de entrada centralizado
- Todas las validaciones y lógica de negocio permanecen en los microservicios 