# Variables para la configuración de Terraform

variable "resource_group_name" {
  description = "Nombre del grupo de recursos"
  type        = string
  default     = "rg-microservicios"
}

variable "location" {
  description = "Región de Azure donde se desplegarán los recursos"
  type        = string
  default     = "East US"
  
  validation {
    condition     = contains([
      "East US", "East US 2", "Central US", "North Central US", "South Central US",
      "West US", "West US 2", "West US 3", "Canada East", "Canada Central",
      "Brazil South", "North Europe", "West Europe", "UK South", "UK West",
      "France Central", "Germany West Central", "Switzerland North", "Norway East",
      "Southeast Asia", "East Asia", "Australia East", "Australia Southeast",
      "Central India", "South India", "West India", "Japan East", "Japan West",
      "Korea Central", "Korea South", "UAE North", "South Africa North"
    ], var.location)
    error_message = "La región especificada no es válida. Consulta la documentación de Azure para regiones válidas."
  }
}

variable "cluster_name" {
  description = "Nombre del cluster AKS"
  type        = string
  default     = "aks-microservicios"
  
  validation {
    condition     = length(var.cluster_name) >= 3 && length(var.cluster_name) <= 63
    error_message = "El nombre del cluster debe tener entre 3 y 63 caracteres."
  }
}

variable "node_count" {
  description = "Número de nodos en el cluster AKS"
  type        = number
  default     = 2
  
  validation {
    condition     = var.node_count >= 1 && var.node_count <= 10
    error_message = "El número de nodos debe estar entre 1 y 10."
  }
}

variable "vm_size" {
  description = "Tamaño de las VMs de los nodos"
  type        = string
  default     = "Standard_D2s_v3"
  
  validation {
    condition     = contains([
      "Standard_B2s", "Standard_B4ms", "Standard_D2s_v3", "Standard_D4s_v3",
      "Standard_D8s_v3", "Standard_E2s_v3", "Standard_E4s_v3", "Standard_E8s_v3"
    ], var.vm_size)
    error_message = "El tamaño de VM especificado no es válido para AKS."
  }
}

variable "kubernetes_version" {
  description = "Versión de Kubernetes para el cluster"
  type        = string
  default     = "1.28.0"
}

variable "postgres_sku" {
  description = "SKU de PostgreSQL"
  type        = string
  default     = "B_Gen5_1"
  
  validation {
    condition     = contains([
      "B_Gen5_1", "B_Gen5_2", "GP_Gen5_2", "GP_Gen5_4", "GP_Gen5_8", "GP_Gen5_16",
      "GP_Gen5_32", "MO_Gen5_2", "MO_Gen5_4", "MO_Gen5_8", "MO_Gen5_16", "MO_Gen5_32"
    ], var.postgres_sku)
    error_message = "El SKU de PostgreSQL especificado no es válido."
  }
}

variable "postgres_storage_mb" {
  description = "Almacenamiento de PostgreSQL en MB"
  type        = number
  default     = 5120
  
  validation {
    condition     = var.postgres_storage_mb >= 5120 && var.postgres_storage_mb <= 1048576
    error_message = "El almacenamiento debe estar entre 5120 MB y 1048576 MB."
  }
}

variable "postgres_backup_retention_days" {
  description = "Días de retención de backup de PostgreSQL"
  type        = number
  default     = 7
  
  validation {
    condition     = var.postgres_backup_retention_days >= 7 && var.postgres_backup_retention_days <= 35
    error_message = "Los días de retención deben estar entre 7 y 35."
  }
}

variable "environment" {
  description = "Ambiente de despliegue"
  type        = string
  default     = "Production"
  
  validation {
    condition     = contains(["Development", "Staging", "Production"], var.environment)
    error_message = "El ambiente debe ser Development, Staging o Production."
  }
}

variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "Microservicios"
}

variable "tags" {
  description = "Tags para los recursos"
  type        = map(string)
  default = {
    Environment = "Production"
    Project     = "Microservicios"
    ManagedBy   = "Terraform"
  }
}

variable "enable_auto_scaling" {
  description = "Habilitar auto-scaling para el cluster AKS"
  type        = bool
  default     = false
}

variable "min_node_count" {
  description = "Número mínimo de nodos para auto-scaling"
  type        = number
  default     = 1
  
  validation {
    condition     = var.min_node_count >= 1
    error_message = "El número mínimo de nodos debe ser al menos 1."
  }
}

variable "max_node_count" {
  description = "Número máximo de nodos para auto-scaling"
  type        = number
  default     = 5
  
  validation {
    condition     = var.max_node_count >= var.min_node_count
    error_message = "El número máximo de nodos debe ser mayor o igual al mínimo."
  }
}

variable "enable_network_policy" {
  description = "Habilitar políticas de red en AKS"
  type        = bool
  default     = true
}

variable "network_plugin" {
  description = "Plugin de red para AKS"
  type        = string
  default     = "azure"
  
  validation {
    condition     = contains(["azure", "kubenet"], var.network_plugin)
    error_message = "El plugin de red debe ser 'azure' o 'kubenet'."
  }
}

variable "service_cidr" {
  description = "CIDR para servicios de Kubernetes"
  type        = string
  default     = "10.0.0.0/16"
}

variable "dns_service_ip" {
  description = "IP del servicio DNS de Kubernetes"
  type        = string
  default     = "10.0.0.10"
}

variable "docker_bridge_cidr" {
  description = "CIDR para el bridge de Docker"
  type        = string
  default     = "172.17.0.1/16"
} 