# Configuración de Kubernetes para los microservicios

# Namespace para los microservicios
resource "kubernetes_namespace" "microservicios" {
  metadata {
    name = "microservicios"
    labels = {
      name = "microservicios"
    }
  }
}

# ConfigMap para variables de entorno comunes
resource "kubernetes_config_map" "common_config" {
  metadata {
    name      = "common-config"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  data = {
    "SPRING_PROFILES_ACTIVE" = "production"
    "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE" = "http://eureka-server:8761/eureka/"
    "SPRING_JPA_HIBERNATE_DDL_AUTO" = "update"
    "SPRING_JPA_SHOW_SQL" = "false"
  }
}

# Secret para credenciales de base de datos
resource "kubernetes_secret" "db_secret" {
  metadata {
    name      = "db-secret"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  data = {
    "POSTGRES_USERNAME" = base64encode("psqladmin")
    "POSTGRES_PASSWORD" = base64encode(azurerm_postgresql_server.postgres_clientes.administrator_login_password)
  }

  type = "Opaque"
}

# Deployment para Eureka Server
resource "kubernetes_deployment" "eureka_server" {
  metadata {
    name      = "eureka-server"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
    labels = {
      app = "eureka-server"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "eureka-server"
      }
    }

    template {
      metadata {
        labels = {
          app = "eureka-server"
        }
      }

      spec {
        container {
          image = "eclipse-temurin:17-jre-alpine"
          name  = "eureka-server"

          port {
            container_port = 8761
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "production"
          }

          env {
            name  = "SERVER_PORT"
            value = "8761"
          }

          env {
            name  = "EUREKA_CLIENT_REGISTER-WITH-EUREKA"
            value = "false"
          }

          env {
            name  = "EUREKA_CLIENT_FETCH-REGISTRY"
            value = "false"
          }

          resources {
            limits = {
              cpu    = "500m"
              memory = "512Mi"
            }
            requests = {
              cpu    = "250m"
              memory = "256Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8761
            }
            initial_delay_seconds = 60
            period_seconds        = 30
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8761
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
        }
      }
    }
  }
}

# Service para Eureka Server
resource "kubernetes_service" "eureka_server" {
  metadata {
    name      = "eureka-server"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  spec {
    selector = {
      app = "eureka-server"
    }

    port {
      port        = 8761
      target_port = 8761
    }

    type = "ClusterIP"
  }
}

# Deployment para Microservicio de Clientes
resource "kubernetes_deployment" "microclientes" {
  metadata {
    name      = "microclientes"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
    labels = {
      app = "microclientes"
    }
  }

  spec {
    replicas = 2

    selector {
      match_labels = {
        app = "microclientes"
      }
    }

    template {
      metadata {
        labels = {
          app = "microclientes"
        }
      }

      spec {
        container {
          image = "eclipse-temurin:17-jre-alpine"
          name  = "microclientes"

          port {
            container_port = 8080
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "production"
          }

          env {
            name  = "SERVER_PORT"
            value = "8080"
          }

          env {
            name  = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
            value = "http://eureka-server:8761/eureka/"
          }

          env {
            name = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://${azurerm_postgresql_server.postgres_clientes.fqdn}:5432/microclientesdb?sslmode=require"
          }

          env {
            name = "SPRING_DATASOURCE_USERNAME"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_secret.metadata[0].name
                key  = "POSTGRES_USERNAME"
              }
            }
          }

          env {
            name = "SPRING_DATASOURCE_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_secret.metadata[0].name
                key  = "POSTGRES_PASSWORD"
              }
            }
          }

          resources {
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
            requests = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 30
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
        }
      }
    }
  }
}

# Service para Microservicio de Clientes
resource "kubernetes_service" "microclientes" {
  metadata {
    name      = "microclientes"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  spec {
    selector = {
      app = "microclientes"
    }

    port {
      port        = 8080
      target_port = 8080
    }

    type = "ClusterIP"
  }
}

# Deployment para Microservicio de Cuentas
resource "kubernetes_deployment" "microcuentas" {
  metadata {
    name      = "microcuentas"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
    labels = {
      app = "microcuentas"
    }
  }

  spec {
    replicas = 2

    selector {
      match_labels = {
        app = "microcuentas"
      }
    }

    template {
      metadata {
        labels = {
          app = "microcuentas"
        }
      }

      spec {
        container {
          image = "eclipse-temurin:17-jre-alpine"
          name  = "microcuentas"

          port {
            container_port = 8081
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "production"
          }

          env {
            name  = "SERVER_PORT"
            value = "8081"
          }

          env {
            name  = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
            value = "http://eureka-server:8761/eureka/"
          }

          env {
            name = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://${azurerm_postgresql_server.postgres_cuentas.fqdn}:5432/microcuentasdb?sslmode=require"
          }

          env {
            name = "SPRING_DATASOURCE_USERNAME"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_secret.metadata[0].name
                key  = "POSTGRES_USERNAME"
              }
            }
          }

          env {
            name = "SPRING_DATASOURCE_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.db_secret.metadata[0].name
                key  = "POSTGRES_PASSWORD"
              }
            }
          }

          env {
            name  = "MICROCLIENTES_SERVICE_URL"
            value = "http://microclientes:8080"
          }

          resources {
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
            requests = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8081
            }
            initial_delay_seconds = 60
            period_seconds        = 30
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8081
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
        }
      }
    }
  }
}

# Service para Microservicio de Cuentas
resource "kubernetes_service" "microcuentas" {
  metadata {
    name      = "microcuentas"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  spec {
    selector = {
      app = "microcuentas"
    }

    port {
      port        = 8081
      target_port = 8081
    }

    type = "ClusterIP"
  }
}

# Deployment para API Gateway
resource "kubernetes_deployment" "gateway" {
  metadata {
    name      = "gateway"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
    labels = {
      app = "gateway"
    }
  }

  spec {
    replicas = 2

    selector {
      match_labels = {
        app = "gateway"
      }
    }

    template {
      metadata {
        labels = {
          app = "gateway"
        }
      }

      spec {
        container {
          image = "eclipse-temurin:17-jre-alpine"
          name  = "gateway"

          port {
            container_port = 8083
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "production"
          }

          env {
            name  = "SERVER_PORT"
            value = "8083"
          }

          env {
            name  = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
            value = "http://eureka-server:8761/eureka/"
          }

          resources {
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
            requests = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8083
            }
            initial_delay_seconds = 60
            period_seconds        = 30
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8083
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
        }
      }
    }
  }
}

# Service para API Gateway
resource "kubernetes_service" "gateway" {
  metadata {
    name      = "gateway"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }

  spec {
    selector = {
      app = "gateway"
    }

    port {
      port        = 8083
      target_port = 8083
    }

    type = "LoadBalancer"
  }
} 