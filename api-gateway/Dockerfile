FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 1. Copy and install the shared library
COPY fleet-commons ./fleet-commons
RUN mvn clean install -f fleet-commons/pom.xml -DskipTests

# 2. Copy and build api-gateway
COPY api-gateway ./api-gateway
RUN mvn clean package -f api-gateway/pom.xml -DskipTests

# Final lightweight stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar
RUN apk add --no-cache wget
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]