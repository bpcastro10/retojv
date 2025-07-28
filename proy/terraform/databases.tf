# Configuración de las bases de datos PostgreSQL

# PostgreSQL para microclientes
resource "azurerm_postgresql_server" "postgres_clientes" {
  name                = "postgres-clientes-${random_string.suffix.result}"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  sku_name = "B_Gen5_1"

  storage_mb                   = 5120
  backup_retention_days        = 7
  geo_redundant_backup_enabled = false
  auto_grow_enabled           = true

  administrator_login          = "psqladmin"
  administrator_login_password = random_password.postgres_password.result
  version                     = "11"
  ssl_enforcement_enabled     = true

  tags = {
    Environment = "Production"
    Project     = "Microservicios"
    Service     = "Clientes"
  }
}

# Base de datos para microclientes
resource "azurerm_postgresql_database" "db_clientes" {
  name                = "microclientesdb"
  resource_group_name = azurerm_resource_group.rg.name
  server_name         = azurerm_postgresql_server.postgres_clientes.name
  charset             = "UTF8"
  collation           = "English_United States.1252"
}

# PostgreSQL para microcuentas
resource "azurerm_postgresql_server" "postgres_cuentas" {
  name                = "postgres-cuentas-${random_string.suffix.result}"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  sku_name = "B_Gen5_1"

  storage_mb                   = 5120
  backup_retention_days        = 7
  geo_redundant_backup_enabled = false
  auto_grow_enabled           = true

  administrator_login          = "psqladmin"
  administrator_login_password = random_password.postgres_password.result
  version                     = "11"
  ssl_enforcement_enabled     = true

  tags = {
    Environment = "Production"
    Project     = "Microservicios"
    Service     = "Cuentas"
  }
}

# Base de datos para microcuentas
resource "azurerm_postgresql_database" "db_cuentas" {
  name                = "microcuentasdb"
  resource_group_name = azurerm_resource_group.rg.name
  server_name         = azurerm_postgresql_server.postgres_cuentas.name
  charset             = "UTF8"
  collation           = "English_United States.1252"
}

# Reglas de firewall para PostgreSQL de clientes
resource "azurerm_postgresql_firewall_rule" "clientes_fw" {
  name                = "allow-aks-clientes"
  resource_group_name = azurerm_resource_group.rg.name
  server_name         = azurerm_postgresql_server.postgres_clientes.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "255.255.255.255"
}

# Reglas de firewall para PostgreSQL de cuentas
resource "azurerm_postgresql_firewall_rule" "cuentas_fw" {
  name                = "allow-aks-cuentas"
  resource_group_name = azurerm_resource_group.rg.name
  server_name         = azurerm_postgresql_server.postgres_cuentas.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "255.255.255.255"
}

# String aleatorio para nombres únicos
resource "random_string" "suffix" {
  length  = 6
  special = false
  upper   = false
}

# Contraseña aleatoria para PostgreSQL
resource "random_password" "postgres_password" {
  length  = 16
  special = true
}

# Outputs para las bases de datos
output "postgres_clientes_server_name" {
  value = azurerm_postgresql_server.postgres_clientes.name
}

output "postgres_cuentas_server_name" {
  value = azurerm_postgresql_server.postgres_cuentas.name
}

output "postgres_admin_username" {
  value = "psqladmin"
}

output "postgres_admin_password" {
  value     = random_password.postgres_password.result
  sensitive = true
} 