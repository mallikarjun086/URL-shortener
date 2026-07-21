# Phase 10: Containerization & Kubernetes Deployment Architecture

## 1. Overview

This application is fully containerized using **Docker** and orchestratable via **Docker Compose** ([docker-compose.yml](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/docker-compose.yml)) for local/development environments, and **Kubernetes** ([deployment.yaml](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/k8s/deployment.yaml)) for production environments.

---

## 2. Multi-Stage Docker Build Architecture

The backend image ([Dockerfile](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/backend/Dockerfile)) utilizes a lightweight two-stage Maven & OpenJDK Alpine build to minimize image size and attack surface:

```dockerfile
# Stage 1: Build JAR
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Kubernetes Production Manifest Topology

The Kubernetes configuration ([deployment.yaml](file:///c:/Users/Mallikarjun%20Gala/OneDrive/Desktop/URL-Shortener/system-design-primer/k8s/deployment.yaml)) specifies:

1. **Deployment**: Manages stateless backend replicas with rolling update strategies zero-downtime deployments.
2. **Horizontal Pod Autoscaler (HPA)**: Automatically scales backend pod count between 3 and 20 replicas based on target CPU utilization (70%) and HTTP throughput thresholds.
3. **Services & Ingress**: Exposes application endpoints externally with SSL/TLS termination.
4. **ConfigMaps & Secrets**: Manages database credentials, Redis connections, and Kafka bootstrap server addresses securely.
