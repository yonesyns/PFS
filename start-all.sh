#!/bin/bash
set -e

echo "🚀 Starting Fleet Management System..."

# Build shared library first
echo "📦 Building fleet-commons..."
cd fleet-commons
mvn clean install -DskipTests
cd ..

# Start infrastructure
echo "🏗️ Starting infrastructure..."
docker-compose up -d redis rabbitmq postgres-customer postgres-vehicle postgres-payment mongodb-document minio

# Wait for infrastructure
echo "⏳ Waiting for infrastructure to be ready..."
sleep 30

# Start services
echo "🚀 Starting microservices..."
docker-compose up -d customer-service vehicle-service document-service payment-service api-gateway

echo "✅ Fleet Management System is starting up!"
echo ""
echo "📍 Access Points:"
echo "   API Gateway:    http://localhost:8080"
echo "   Customer API:   http://localhost:8081/swagger-ui.html"
echo "   Vehicle API:    http://localhost:8082/swagger-ui.html"
echo "   Document API:   http://localhost:8083/swagger-ui.html"
echo "   Payment API:    http://localhost:8084/swagger-ui.html"
echo "   RabbitMQ UI:    http://localhost:15672 (fleet_user/fleet_password)"
echo "   MinIO Console:  http://localhost:9001 (minioadmin/minioadmin)"
