FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 1. Copy and install the shared library
COPY fleet-commons ./fleet-commons
RUN mvn clean install -f fleet-commons/pom.xml -DskipTests

# 2. Copy and build customer-service
COPY customer-service ./customer-service
RUN mvn clean package -f customer-service/pom.xml -DskipTests

# Final lightweight stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/customer-service/target/*.jar app.jar
RUN apk add --no-cache wget
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]