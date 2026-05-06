FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/customer-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]