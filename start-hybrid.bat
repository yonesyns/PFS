@echo off
echo Starting Fleet Management - Hybrid Mode
echo.

echo Step 1: Starting infrastructure with Docker...
docker-compose up -d redis zookeeper kafka prometheus grafana zipkin
if %errorlevel% neq 0 (
    echo ERROR: Docker failed to start infrastructure
    echo Please check Docker Desktop is running
    pause
    exit /b 1
)

echo Waiting for infrastructure to be ready...
timeout /t 30

echo Step 2: Building Java services...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

echo Step 3: Starting Java services...
echo Starting Eureka Server...
start "Eureka Server" java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

echo Waiting for Eureka...
timeout /t 30

echo Starting Config Server...
start "Config Server" java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar

echo Waiting for Config Server...
timeout /t 20

echo Starting API Gateway...
start "API Gateway" java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar

echo Waiting for API Gateway...
timeout /t 20

echo Starting Microservices...
start "Customer Service" java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
start "Vehicle Service" java -jar vehicle-service/target/vehicle-service-0.0.1-SNAPSHOT.jar
start "Payment Service" java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
start "Document Service" java -jar document-service/target/document-service-0.0.1-SNAPSHOT.jar

echo.
echo ========================================
echo Fleet Management System Started!
echo ========================================
echo.
echo Access Points:
echo - API Gateway: http://localhost:8080
echo - Eureka Dashboard: http://localhost:8761
echo - Grafana: http://localhost:3000 (admin/admin)
echo - Prometheus: http://localhost:9090
echo.
echo Wait 2-3 minutes for all services to be ready
echo Then test with Bruno: d:\PFS\bruno-tests
echo.
pause