@echo off
echo ========================================
echo Fleet Management - Mode Local Complet
echo ========================================
echo.

echo Step 1: Building all services...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

echo.
echo Step 2: Starting services in order...
echo.

echo [1/7] Starting Eureka Server (Service Discovery)...
start "Eureka Server" cmd /k "echo Eureka Server && java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar"

echo Waiting for Eureka to start...
timeout /t 45

echo [2/7] Starting Config Server...
start "Config Server" cmd /k "echo Config Server && java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar"

echo Waiting for Config Server...
timeout /t 30

echo [3/7] Starting API Gateway...
start "API Gateway" cmd /k "echo API Gateway && java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar"

echo Waiting for API Gateway...
timeout /t 30

echo [4/7] Starting Customer Service...
start "Customer Service" cmd /k "echo Customer Service && java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar"

echo [5/7] Starting Vehicle Service...
start "Vehicle Service" cmd /k "echo Vehicle Service && java -jar vehicle-service/target/vehicle-service-0.0.1-SNAPSHOT.jar"

echo [6/7] Starting Payment Service...
start "Payment Service" cmd /k "echo Payment Service && java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar"

echo [7/7] Starting Document Service...
start "Document Service" cmd /k "echo Document Service && java -jar document-service/target/document-service-0.0.1-SNAPSHOT.jar"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Wait 2-3 minutes for all services to be ready
echo.
echo Access Points:
echo - API Gateway: http://localhost:8080
echo - Eureka Dashboard: http://localhost:8761
echo - Customer Service: http://localhost:8081
echo - Vehicle Service: http://localhost:8082
echo - Payment Service: http://localhost:8083
echo - Document Service: http://localhost:8084
echo.
echo H2 Database Consoles:
echo - Customer DB: http://localhost:8081/h2-console
echo - Vehicle DB: http://localhost:8082/h2-console
echo - Payment DB: http://localhost:8083/h2-console
echo - Document DB: http://localhost:8084/h2-console
echo   (JDBC URL: jdbc:h2:mem:xxxdb, User: sa, Password: password)
echo.
echo Ready for Bruno testing: d:\PFS\bruno-tests
echo.
pause