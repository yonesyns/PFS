FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 1. Copy and install the shared library
COPY fleet-commons ./fleet-commons
RUN mvn clean install -f fleet-commons/pom.xml -DskipTests

# 2. Copy and build document-service
COPY document-service ./document-service
RUN mvn clean package -f document-service/pom.xml -DskipTests

# Final lightweight stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/document-service/target/*.jar app.jar
RUN apk add --no-cache wget
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]