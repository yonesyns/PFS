@echo off
echo Building Fleet Management Microservices...

echo Building all services from parent POM...
call mvn clean package -DskipTests

echo All services built successfully!
echo Run 'docker-compose up' to start the application.