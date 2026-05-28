# ==========================================
# STAGE 1: Dependency caching layer
# This layer only rebuilds when pom.xml files change
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS deps
WORKDIR /build

# Copy ALL pom files first (for optimal layer caching)
COPY pom.xml .
COPY fleet-commons/pom.xml    ./fleet-commons/
COPY customer-service/pom.xml ./customer-service/
COPY vehicle-service/pom.xml  ./vehicle-service/
COPY document-service/pom.xml ./document-service/
COPY payment-service/pom.xml  ./payment-service/
COPY api-gateway/pom.xml      ./api-gateway/

# Download all dependencies without building
# This layer is cached as long as pom.xml files don't change
RUN mvn dependency:go-offline -B --no-transfer-progress

# ==========================================
# STAGE 2: Build all modules
# ==========================================
FROM deps AS builder
WORKDIR /build

# Copy source code (this layer rebuilds when code changes)
COPY fleet-commons/    ./fleet-commons/
COPY customer-service/ ./customer-service/
COPY vehicle-service/  ./vehicle-service/
COPY document-service/ ./document-service/
COPY payment-service/  ./payment-service/
COPY api-gateway/      ./api-gateway/

# Build shared library first, then all services in one pass
RUN mvn clean install -pl fleet-commons -am -DskipTests --no-transfer-progress
RUN mvn clean package \
    -pl customer-service,vehicle-service,document-service,payment-service,api-gateway \
    -DskipTests \
    --no-transfer-progress

# ==========================================
# STAGE 3: Runtime — Customer Service
# ==========================================
FROM eclipse-temurin:21-jre-jammy AS customer-service
WORKDIR /app

# Security: run as non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /build/customer-service/target/*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==========================================
# STAGE 4: Runtime — Vehicle Service
# ==========================================
FROM eclipse-temurin:21-jre-jammy AS vehicle-service
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /build/vehicle-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==========================================
# STAGE 5: Runtime — Document Service
# ==========================================
FROM eclipse-temurin:21-jre-jammy AS document-service
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /build/document-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==========================================
# STAGE 6: Runtime — Payment Service
# ==========================================
FROM eclipse-temurin:21-jre-jammy AS payment-service
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /build/payment-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==========================================
# STAGE 7: Runtime — API Gateway
# ==========================================
FROM eclipse-temurin:21-jre-jammy AS api-gateway
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /build/api-gateway/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]