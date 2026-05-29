# Fleet Management - Windows Start Script
# Requires: Docker Desktop, Java 21, Maven 3.9+

Write-Host "🚀 Fleet Management System - Windows Setup" -ForegroundColor Green

# Check Docker
Write-Host "`n📦 Checking Docker..." -ForegroundColor Cyan
try {
    $dockerInfo = docker info 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Docker is not running! Please start Docker Desktop first." -ForegroundColor Red
        Write-Host "   1. Open Docker Desktop from Start Menu" -ForegroundColor Yellow
        Write-Host "   2. Wait for the whale icon to stop animating" -ForegroundColor Yellow
        Write-Host "   3. Run this script again" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker not found! Install Docker Desktop: https://docs.docker.com/desktop/install/windows-install/" -ForegroundColor Red
    exit 1
}

# Check Java
Write-Host "`n☕ Checking Java..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-String "openjdk version" | ForEach-Object { $_ -replace '.*"(.*)".*','$1' }
    if ($javaVersion -match "21") {
        Write-Host "✅ Java 21 found: $javaVersion" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Java version might not be 21. Found: $javaVersion" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Java not found! Install Java 21: https://adoptium.net/" -ForegroundColor Red
    exit 1
}

# Check Maven
Write-Host "`n📐 Checking Maven..." -ForegroundColor Cyan
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven" | ForEach-Object { $_.ToString().Split()[2] }
    Write-Host "✅ Maven found: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found! Install Maven: https://maven.apache.org/download.cgi" -ForegroundColor Red
    exit 1
}

# Build fleet-commons first
Write-Host "`n📦 Building fleet-commons (shared library)..." -ForegroundColor Cyan
Set-Location -Path "fleet-commons"
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to build fleet-commons" -ForegroundColor Red
    Set-Location -Path ".."
    exit 1
}
Set-Location -Path ".."
Write-Host "✅ fleet-commons built successfully" -ForegroundColor Green

# Start infrastructure only first
Write-Host "`n🏗️ Starting infrastructure services..." -ForegroundColor Cyan
docker-compose up -d redis rabbitmq postgres-auth postgres-customer postgres-vehicle postgres-payment mongodb-document minio

Write-Host "`n⏳ Waiting for infrastructure to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check infrastructure health
Write-Host "`n🔍 Checking infrastructure health..." -ForegroundColor Cyan
$services = @("fleet-redis", "fleet-rabbitmq", "fleet-postgres-auth", "fleet-postgres-customer", "fleet-postgres-vehicle", "fleet-postgres-payment", "fleet-mongodb-document", "fleet-minio")
foreach ($svc in $services) {
    $status = docker inspect --format='{{.State.Status}}' $svc 2>$null
    if ($status -eq "running") {
        Write-Host "   ✅ $svc is running" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️ $svc status: $status" -ForegroundColor Yellow
    }
}

# Start microservices
Write-Host "`n🚀 Starting microservices..." -ForegroundColor Cyan
docker-compose up -d auth-service customer-service vehicle-service document-service payment-service api-gateway

Write-Host "`n⏳ Waiting for services to start (60 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Final status
Write-Host "`n📊 Service Status:" -ForegroundColor Cyan
$allServices = @("fleet-api-gateway", "fleet-auth-service", "fleet-customer-service", "fleet-vehicle-service", "fleet-document-service", "fleet-payment-service")
foreach ($svc in $allServices) {
    $status = docker inspect --format='{{.State.Status}}' $svc 2>$null
    $health = docker inspect --format='{{.State.Health.Status}}' $svc 2>$null
    if ($status -eq "running") {
        Write-Host "   ✅ $svc - Running (Health: $health)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $svc - $status" -ForegroundColor Red
    }
}

Write-Host "`n🎉 Setup complete!" -ForegroundColor Green
Write-Host "`n📍 Access Points:" -ForegroundColor Cyan
Write-Host "   API Gateway:    http://localhost:8080" -ForegroundColor White
Write-Host "   Auth API:       http://localhost:8085/swagger-ui.html" -ForegroundColor White
Write-Host "   Customer API:   http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "   Vehicle API:    http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host "   Document API:   http://localhost:8083/swagger-ui.html" -ForegroundColor White
Write-Host "   Payment API:    http://localhost:8084/swagger-ui.html" -ForegroundColor White
Write-Host "   RabbitMQ UI:    http://localhost:15672 (fleet_user / fleet_password)" -ForegroundColor White
Write-Host "   MinIO Console:  http://localhost:9001 (minioadmin / minioadmin)" -ForegroundColor White

Write-Host "`n🛠️ Useful Commands:" -ForegroundColor Cyan
Write-Host "   View logs:        docker-compose logs -f [service-name]" -ForegroundColor DarkGray
Write-Host "   Stop all:         docker-compose down" -ForegroundColor DarkGray
Write-Host "   Restart service:  docker-compose restart [service-name]" -ForegroundColor DarkGray
Write-Host "   View stats:       docker stats" -ForegroundColor DarkGray
