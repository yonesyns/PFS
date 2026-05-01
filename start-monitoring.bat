@echo off
echo Starting Fleet Management Monitoring Stack...

echo Starting infrastructure services...
docker-compose up -d redis redis-exporter prometheus grafana zipkin

echo Waiting for services to be ready...
timeout /t 30

echo Starting Kafka and Zookeeper...
docker-compose up -d zookeeper kafka

echo Waiting for Kafka to be ready...
timeout /t 20

echo Starting application services...
docker-compose up -d eureka-server config-server

echo Waiting for core services...
timeout /t 30

echo Starting microservices...
docker-compose up -d api-gateway customer-service vehicle-service payment-service document-service

echo All services started!
echo.
echo Access points:
echo - API Gateway: http://localhost:8080
echo - Eureka Dashboard: http://localhost:8761
echo - Grafana: http://localhost:3000 (admin/admin)
echo - Prometheus: http://localhost:9090
echo - Zipkin: http://localhost:9411
echo - Redis: localhost:6379
echo.
echo Monitoring is ready!