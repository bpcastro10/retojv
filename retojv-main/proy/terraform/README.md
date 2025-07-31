# 🚀 Despliegue en Azure Kubernetes Service (AKS) con Terraform

Este directorio contiene la configuración de Terraform para desplegar la infraestructura completa de microservicios en Azure Kubernetes Service (AKS).

## 📋 Arquitectura Desplegada

```
┌─────────────────────────────────────────────────────────────┐
│                        AZURE CLOUD                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   AKS CLUSTER   │    │  POSTGRESQL     │                │
│  │                 │    │   DATABASES     │                │
│  │ ┌─────────────┐ │    │                 │                │
│  │ │ Eureka      │ │    │ ┌─────────────┐ │                │
│  │ │ Server      │ │    │ │ Clientes DB │ │                │
│  │ └─────────────┘ │    │ └─────────────┘ │                │
│  │                 │    │ ┌─────────────┐ │                │
│  │ ┌─────────────┐ │    │ │ Cuentas DB  │ │                │
│  │ │ API Gateway │ │    │ └─────────────┘ │                │
│  │ └─────────────┘ │    └─────────────────┘                │
│  │                 │                                       │
│  │ ┌─────────────┐ │                                       │
│  │ │Microclientes│ │                                       │
│  │ └─────────────┘ │                                       │
│  │                 │                                       │
│  │ ┌─────────────┐ │                                       │
│  │ │Microcuentas │ │                                       │
│  │ └─────────────┘ │                                       │
│  └─────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Prerrequisitos

### Software Requerido

1. **Azure CLI** (versión 2.0 o superior)
   ```bash
   # Windows (con Chocolatey)
   choco install azure-cli
   
   # macOS (con Homebrew)
   brew install azure-cli
   
   # Linux
   curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
   ```

2. **Terraform** (versión 1.0 o superior)
   ```bash
   # Windows (con Chocolatey)
   choco install terraform
   
   # macOS (con Homebrew)
   brew install terraform
   
   # Linux
   wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
   echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
   sudo apt update && sudo apt install terraform
   ```

3. **kubectl** (opcional, se instala automáticamente)
   ```bash
   # Windows (con Chocolatey)
   choco install kubernetes-cli
   
   # macOS (con Homebrew)
   brew install kubernetes-cli
   
   # Linux
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
   sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
   ```

### Cuenta de Azure

1. **Suscripción de Azure** activa
2. **Permisos** para crear recursos (Contributor o Owner)
3. **Autenticación** configurada:
   ```bash
   az login
   ```

## 📁 Estructura de Archivos

```
terraform/
├── main.tf                 # Configuración principal y cluster AKS
├── databases.tf            # Bases de datos PostgreSQL
├── kubernetes.tf           # Manifiestos de Kubernetes
├── variables.tf            # Definición de variables
├── outputs.tf              # Outputs del despliegue
├── terraform.tfvars.example # Ejemplo de configuración
├── deploy.sh               # Script de despliegue (Linux/Mac)
├── deploy.ps1              # Script de despliegue (Windows)
└── README.md               # Este archivo
```

## 🚀 Despliegue Rápido

### Opción 1: Script Automático (Recomendado)

#### En Windows (PowerShell):
```powershell
# Con confirmaciones
.\deploy.ps1

# Sin confirmaciones (para CI/CD)
.\deploy.ps1 -SkipConfirmation
```

#### En Linux/Mac (Bash):
```bash
# Hacer ejecutable el script
chmod +x deploy.sh

# Ejecutar el despliegue
./deploy.sh
```

### Opción 2: Comandos Manuales

1. **Configurar variables:**
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   # Editar terraform.tfvars con tus valores
   ```

2. **Inicializar Terraform:**
   ```bash
   terraform init
   ```

3. **Planificar el despliegue:**
   ```bash
   terraform plan -out=tfplan
   ```

4. **Aplicar el despliegue:**
   ```bash
   terraform apply tfplan
   ```

5. **Configurar kubectl:**
   ```bash
   az aks get-credentials --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw kubernetes_cluster_name)
   ```

## ⚙️ Configuración

### Variables Principales

Edita `terraform.tfvars` para personalizar tu despliegue:

```hcl
# Configuración básica
resource_group_name = "rg-microservicios"
location            = "East US"
cluster_name        = "aks-microservicios"

# Cluster AKS
node_count          = 2
vm_size             = "Standard_D2s_v3"
kubernetes_version  = "1.28.0"

# Bases de datos
postgres_sku                    = "B_Gen5_1"
postgres_storage_mb             = 5120
postgres_backup_retention_days  = 7

# Ambiente
environment  = "Production"
project_name = "Microservicios"
```

### Regiones Disponibles

- **América del Norte:** East US, East US 2, Central US, West US, West US 2
- **Europa:** North Europe, West Europe, UK South, France Central
- **Asia Pacífico:** Southeast Asia, East Asia, Australia East
- **Otros:** Brazil South, Canada Central, Japan East

### Tamaños de VM Recomendados

| Uso | Tamaño | CPU | RAM | Costo/Mes |
|-----|--------|-----|-----|-----------|
| Desarrollo | Standard_B2s | 2 | 4GB | ~$30 |
| Pruebas | Standard_D2s_v3 | 2 | 8GB | ~$73 |
| Producción | Standard_D4s_v3 | 4 | 16GB | ~$146 |

## 📊 Recursos Creados

### Infraestructura Base
- **Grupo de recursos** con tags organizacionales
- **Red virtual** con subnets separadas
- **Cluster AKS** con auto-scaling
- **2 servidores PostgreSQL** (clientes y cuentas)

### Kubernetes
- **Namespace** `microservicios`
- **ConfigMaps** para configuración
- **Secrets** para credenciales
- **Deployments** para cada microservicio
- **Services** para comunicación interna
- **LoadBalancer** para el API Gateway

### Seguridad
- **Identidad administrada** para AKS
- **Políticas de red** habilitadas
- **SSL/TLS** para conexiones de base de datos
- **Firewall** configurado para PostgreSQL

## 💰 Estimación de Costos

### Costos Mensuales (aproximados)

| Recurso | Especificación | Costo/Mes |
|---------|----------------|-----------|
| AKS Cluster | 2 nodos Standard_D2s_v3 | ~$146 |
| Load Balancer | Básico | ~$18 |
| PostgreSQL Clientes | B_Gen5_1 | ~$25 |
| PostgreSQL Cuentas | B_Gen5_1 | ~$25 |
| Red Virtual | VNET + Subnets | ~$5 |
| **TOTAL** | | **~$219** |

### Optimización de Costos

1. **Desarrollo:** Usar nodos más pequeños (Standard_B2s)
2. **Auto-scaling:** Habilitar para reducir costos en horas de bajo uso
3. **Reserved Instances:** Para cargas de trabajo estables
4. **Spot Instances:** Para cargas de trabajo tolerantes a interrupciones

## 🔧 Comandos Útiles

### Gestión de Terraform
```bash
# Ver estado
terraform show

# Ver outputs
terraform output

# Ver outputs sensibles
terraform output postgres_admin_password

# Destruir infraestructura
terraform destroy

# Actualizar configuración
terraform plan
terraform apply
```

### Gestión de Kubernetes
```bash
# Ver pods
kubectl get pods -n microservicios

# Ver servicios
kubectl get services -n microservicios

# Ver logs
kubectl logs -f deployment/eureka-server -n microservicios

# Escalar deployments
kubectl scale deployment microclientes --replicas=3 -n microservicios

# Port forwarding
kubectl port-forward svc/gateway 8083:8083 -n microservicios
```

### Gestión de Azure
```bash
# Ver recursos
az resource list --resource-group $(terraform output -raw resource_group_name)

# Ver cluster AKS
az aks show --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw kubernetes_cluster_name)

# Conectar a cluster
az aks get-credentials --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw kubernetes_cluster_name)
```

## 🔒 Seguridad

### Mejores Prácticas Implementadas

1. **Identidad administrada** para AKS
2. **Políticas de red** habilitadas
3. **Secrets de Kubernetes** para credenciales
4. **SSL/TLS** para conexiones de base de datos
5. **Firewall** configurado para PostgreSQL
6. **Tags** para organización y facturación

### Recomendaciones Adicionales

1. **Azure Key Vault** para gestión de secretos
2. **Azure Application Gateway** para WAF
3. **Azure Monitor** para monitoreo
4. **Azure Backup** para respaldos
5. **Network Security Groups** más restrictivos

## 🐛 Solución de Problemas

### Problemas Comunes

#### Error: "Insufficient quota"
```bash
# Verificar cuotas
az vm list-usage --location "East US" --output table

# Solicitar aumento de cuota
az vm list-usage --location "East US" --query "[?contains(name.value, 'standardDSv3Family')]"
```

#### Error: "Resource group already exists"
```bash
# Cambiar nombre en terraform.tfvars
resource_group_name = "rg-microservicios-$(date +%s)"
```

#### Error: "Cluster name already exists"
```bash
# Cambiar nombre en terraform.tfvars
cluster_name = "aks-microservicios-$(date +%s)"
```

#### Problemas de conectividad
```bash
# Verificar autenticación
az account show

# Reautenticar si es necesario
az login
```

### Logs y Diagnóstico

```bash
# Logs de Terraform
terraform plan -detailed-exitcode

# Logs de AKS
az aks show --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw kubernetes_cluster_name) --query "diagnosticsProfile"

# Logs de pods
kubectl logs -f deployment/eureka-server -n microservicios
```

## 📈 Monitoreo y Alertas

### Configuración Recomendada

1. **Azure Monitor** para métricas del cluster
2. **Application Insights** para los microservicios
3. **Log Analytics** para logs centralizados
4. **Alertas** para métricas críticas

### Métricas Importantes

- **CPU y memoria** de los nodos
- **Latencia** de las bases de datos
- **Throughput** de las APIs
- **Errores** de los servicios

## 🚀 Próximos Pasos

### Después del Despliegue

1. **Construir y subir imágenes Docker:**
   ```bash
   # Crear Azure Container Registry
   az acr create --resource-group $(terraform output -raw resource_group_name) --name myregistry --sku Basic
   
   # Construir y subir imágenes
   docker build -t myregistry.azurecr.io/eureka-server:latest ../eureka-server/
   az acr build --registry myregistry --image eureka-server:latest ../eureka-server/
   ```

2. **Aplicar manifiestos de Kubernetes:**
   ```bash
   kubectl apply -f k8s-manifests/
   ```

3. **Configurar dominio personalizado:**
   ```bash
   # Configurar Application Gateway o Azure Front Door
   ```

4. **Configurar CI/CD:**
   ```bash
   # Configurar Azure DevOps o GitHub Actions
   ```

### Mejoras Futuras

1. **Multi-región** para alta disponibilidad
2. **Kubernetes HPA** para auto-scaling
3. **Istio** para service mesh
4. **Prometheus + Grafana** para monitoreo
5. **Falco** para seguridad en tiempo real

## 📞 Soporte

### Recursos Útiles

- [Documentación de Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Documentación de AKS](https://docs.microsoft.com/en-us/azure/aks/)
- [Documentación de Azure PostgreSQL](https://docs.microsoft.com/en-us/azure/postgresql/)

### Contacto

Si encuentras problemas:

1. Revisa los logs con `terraform plan` y `kubectl logs`
2. Verifica la configuración en `terraform.tfvars`
3. Asegúrate de tener permisos suficientes en Azure
4. Consulta la documentación oficial

---

**¡Disfruta desplegando en Azure! 🎉** 