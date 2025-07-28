# Outputs para mostrar información importante después del despliegue

output "resource_group_name" {
  description = "Nombre del grupo de recursos creado"
  value       = azurerm_resource_group.rg.name
}

output "resource_group_location" {
  description = "Ubicación del grupo de recursos"
  value       = azurerm_resource_group.rg.location
}

output "kubernetes_cluster_name" {
  description = "Nombre del cluster AKS"
  value       = azurerm_kubernetes_cluster.aks.name
}

output "kubernetes_cluster_version" {
  description = "Versión de Kubernetes del cluster"
  value       = azurerm_kubernetes_cluster.aks.kubernetes_version
}

output "kubernetes_cluster_endpoint" {
  description = "Endpoint del cluster Kubernetes"
  value       = azurerm_kubernetes_cluster.aks.kube_config.0.host
}

output "kubernetes_cluster_identity" {
  description = "Identidad del cluster AKS"
  value       = azurerm_kubernetes_cluster.aks.identity
}

output "postgres_clientes_server_name" {
  description = "Nombre del servidor PostgreSQL para clientes"
  value       = azurerm_postgresql_server.postgres_clientes.name
}

output "postgres_clientes_server_fqdn" {
  description = "FQDN del servidor PostgreSQL para clientes"
  value       = azurerm_postgresql_server.postgres_clientes.fqdn
}

output "postgres_cuentas_server_name" {
  description = "Nombre del servidor PostgreSQL para cuentas"
  value       = azurerm_postgresql_server.postgres_cuentas.name
}

output "postgres_cuentas_server_fqdn" {
  description = "FQDN del servidor PostgreSQL para cuentas"
  value       = azurerm_postgresql_server.postgres_cuentas.fqdn
}

output "postgres_admin_username" {
  description = "Usuario administrador de PostgreSQL"
  value       = "psqladmin"
}

output "postgres_admin_password" {
  description = "Contraseña del administrador de PostgreSQL"
  value       = random_password.postgres_password.result
  sensitive   = true
}

output "virtual_network_name" {
  description = "Nombre de la red virtual"
  value       = azurerm_virtual_network.vnet.name
}

output "virtual_network_id" {
  description = "ID de la red virtual"
  value       = azurerm_virtual_network.vnet.id
}

output "aks_subnet_id" {
  description = "ID de la subnet de AKS"
  value       = azurerm_subnet.aks_subnet.id
}

output "postgres_subnet_id" {
  description = "ID de la subnet de PostgreSQL"
  value       = azurerm_subnet.postgres_subnet.id
}

output "kubernetes_namespace" {
  description = "Namespace de Kubernetes para los microservicios"
  value       = kubernetes_namespace.microservicios.metadata[0].name
}

output "connection_strings" {
  description = "Cadenas de conexión para las bases de datos"
  value = {
    clientes = "jdbc:postgresql://${azurerm_postgresql_server.postgres_clientes.fqdn}:5432/microclientesdb?sslmode=require"
    cuentas  = "jdbc:postgresql://${azurerm_postgresql_server.postgres_cuentas.fqdn}:5432/microcuentasdb?sslmode=require"
  }
  sensitive = true
}

output "kube_config" {
  description = "Configuración de kubectl para conectarse al cluster"
  value       = azurerm_kubernetes_cluster.aks.kube_config_raw
  sensitive   = true
}

output "deployment_instructions" {
  description = "Instrucciones para desplegar los microservicios"
  value = <<-EOT
    ========================================
    🚀 DESPLIEGUE COMPLETADO EN AZURE AKS
    ========================================
    
    📋 INFORMACIÓN DEL CLUSTER:
    • Nombre del cluster: ${azurerm_kubernetes_cluster.aks.name}
    • Versión de Kubernetes: ${azurerm_kubernetes_cluster.aks.kubernetes_version}
    • Grupo de recursos: ${azurerm_resource_group.rg.name}
    • Región: ${azurerm_resource_group.rg.location}
    
    🗄️ BASES DE DATOS:
    • PostgreSQL Clientes: ${azurerm_postgresql_server.postgres_clientes.fqdn}
    • PostgreSQL Cuentas: ${azurerm_postgresql_server.postgres_cuentas.fqdn}
    • Usuario: psqladmin
    • Contraseña: [Ver en outputs sensibles]
    
    🔧 PRÓXIMOS PASOS:
    1. Configurar kubectl:
       az aks get-credentials --resource-group ${azurerm_resource_group.rg.name} --name ${azurerm_kubernetes_cluster.aks.name}
    
    2. Construir y subir las imágenes Docker:
       docker build -t [registry].azurecr.io/eureka-server:latest ../eureka-server/
       docker build -t [registry].azurecr.io/microclientes:latest ../microclientes/microclientes/
       docker build -t [registry].azurecr.io/microcuentas:latest ../microcuentas/microcuentas/
       docker build -t [registry].azurecr.io/gateway:latest ../gateway/gateway/
    
    3. Aplicar los manifiestos de Kubernetes:
       kubectl apply -f k8s-manifests/
    
    4. Verificar el estado:
       kubectl get pods -n microservicios
       kubectl get services -n microservicios
    
    📊 MONITOREO:
    • Eureka Server: kubectl port-forward svc/eureka-server 8761:8761 -n microservicios
    • API Gateway: kubectl port-forward svc/gateway 8083:8083 -n microservicios
    
    🔒 SEGURIDAD:
    • Las credenciales de base de datos están en Kubernetes Secrets
    • Los servicios usan SSL/TLS para conexiones de base de datos
    • Las políticas de red están habilitadas
    
    ========================================
  EOT
}

output "cost_estimate" {
  description = "Estimación de costos mensuales (aproximada)"
  value = <<-EOT
    💰 ESTIMACIÓN DE COSTOS MENSUALES:
    
    AKS Cluster (2 nodos Standard_D2s_v3):
    • Nodos: ~$146 USD/mes
    • Load Balancer: ~$18 USD/mes
    
    PostgreSQL (2 instancias B_Gen5_1):
    • Clientes: ~$25 USD/mes
    • Cuentas: ~$25 USD/mes
    
    Red Virtual:
    • VNET + Subnets: ~$5 USD/mes
    
    TOTAL ESTIMADO: ~$219 USD/mes
    
    ⚠️  Los costos reales pueden variar según el uso y la región.
    💡 Para reducir costos, considera usar nodos más pequeños en desarrollo.
  EOT
} 