# Fleet Management Microservices

A comprehensive microservices-based fleet management system built with Spring Boot, featuring service discovery, API gateway, distributed tracing, and monitoring.

## Architecture

### Microservices
- **Customer Service** (Port 8081) - Customer management
- **Vehicle Service** (Port 8082) - Vehicle fleet management  
- **Payment Service** (Port 8083) - Payment processing
- **Document Service** (Port 8084) - Document management

### Infrastructure Services
- **Eureka Server** (Port 8761) - Service discovery
- **Config Server** (Port 8888) - Centralized configuration
- **API Gateway** (Port 8080) - Routing and rate limiting
- **Redis** (Port 6379) - Caching and rate limiting
- **Kafka** (Port 9092) - Async messaging
- **Zipkin** (Port 9411) - Distributed tracing
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3000) - Monitoring dashboards

## Features

### Core Features
- ✅ Service Discovery with Eureka
- ✅ API Gateway with routing and rate limiting
- ✅ Centralized configuration with Config Server
- ✅ Redis caching
- ✅ Kafka async messaging
- ✅ JWT security
- ✅ Distributed tracing with Zipkin
- ✅ Monitoring with Prometheus + Grafana
- ✅ Docker Compose for local development
- ✅ Kubernetes configurations for production

### API Endpoints

#### Customer Service
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create customer
- `PUT /api/customers/{id}` - Update customer

#### Vehicle Service  
- `GET /api/vehicles` - Get all vehicles
- `GET /api/vehicles/{id}` - Get vehicle by ID
- `GET /api/vehicles/available` - Get available vehicles
- `POST /api/vehicles` - Create vehicle
- `PUT /api/vehicles/{id}` - Update vehicle

#### Payment Service
- `POST /api/payments` - Process payment
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments/customer/{customerId}` - Get payments by customer
- `GET /api/payments/booking/{bookingId}` - Get payments by booking

#### Document Service
- `POST /api/documents/upload` - Upload document
- `GET /api/documents/{id}` - Get document by ID
- `GET /api/documents/entity/{entityId}` - Get documents by entity
- `PUT /api/documents/{id}/status` - Update document status
- `DELETE /api/documents/{id}` - Delete document

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Local Development

1. **Build all services:**
   ```bash
   ./build-all.bat
   ```

2. **Start infrastructure services:**
   ```bash
   docker-compose up -d redis kafka zookeeper zipkin prometheus grafana
   ```

3. **Start application services:**
   ```bash
   # Start in order
   java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
   java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
   java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
   java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
   java -jar vehicle-service/target/vehicle-service-0.0.1-SNAPSHOT.jar
   java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
   java -jar document-service/target/document-service-0.0.1-SNAPSHOT.jar
   ```

4. **Or use Docker Compose for everything:**
   ```bash
   docker-compose up
   ```

### Access Points
- **API Gateway:** http://localhost:8080
- **Eureka Dashboard:** http://localhost:8761
- **Zipkin:** http://localhost:9411
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (admin/admin)

### Kubernetes Deployment

1. **Create namespace:**
   ```bash
   kubectl apply -f k8s/namespace.yaml
   ```

2. **Deploy services:**
   ```bash
   kubectl apply -f k8s/
   ```

## Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE` - Active profile (local, docker, k8s)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - Eureka server URL
- `SPRING_CONFIG_IMPORT` - Config server URL
- `SPRING_REDIS_HOST` - Redis host
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka brokers

### Rate Limiting
API Gateway includes rate limiting:
- Customer/Vehicle services: 10 requests/second, burst 20
- Payment/Document services: 5 requests/second, burst 10

### Security
JWT tokens required for protected endpoints. Include in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Monitoring

### Metrics
All services expose Prometheus metrics at `/actuator/prometheus`

### Tracing  
Distributed tracing with Zipkin. All requests are traced across services.

### Health Checks
Health endpoints available at `/actuator/health` for each service.

## Development

### Adding New Service
1. Create new Spring Boot project with required dependencies
2. Add Eureka client configuration
3. Configure in API Gateway routes
4. Add Kubernetes deployment files
5. Update Docker Compose

### Testing
```bash
# Run tests for all services
mvn test

# Integration tests with Testcontainers
mvn verify
```

## Production Considerations

### Scaling
- Use Kubernetes HPA for auto-scaling
- Configure resource limits and requests
- Use persistent volumes for document storage

### Security
- Use proper JWT secret management
- Enable HTTPS/TLS
- Configure network policies
- Use secrets for sensitive configuration

### Monitoring
- Set up alerting rules in Prometheus
- Configure Grafana dashboards
- Enable log aggregation (ELK stack)
- Monitor business metrics

## Troubleshooting

### Common Issues
1. **Service not registering with Eureka**
   - Check network connectivity
   - Verify Eureka server is running
   - Check service configuration

2. **Rate limiting issues**
   - Verify Redis connection
   - Check rate limit configuration
   - Monitor Redis memory usage

3. **Kafka connection issues**
   - Ensure Kafka and Zookeeper are running
   - Check bootstrap servers configuration
   - Verify topic creation

### Logs
Check service logs for detailed error information:
```bash
docker-compose logs <service-name>
kubectl logs -f deployment/<service-name> -n fleet-management
```