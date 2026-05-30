# 🚗 Fleet Management System - B2B Microservices

A complete B2B Fleet Management platform built with Spring Boot microservices architecture.

## 📐 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     SPRING CLOUD GATEWAY                        │
│                     Port: 8080                                  │
└──────────────┬──────────────┬──────────────┬────────────────────┘
               │              │              │
        ┌──────▼──────┐ ┌─────▼──────┐ ┌────▼─────┐ ┌──────────▼──┐
        │   CUSTOMER  │ │  VEHICLE   │ │ DOCUMENT │ │   PAYMENT   │
        │   SERVICE   │ │  SERVICE   │ │ SERVICE  │ │   SERVICE   │
        │  Port: 8081 │ │  Port:8082 │ │ Port:8083│ │  Port:8084  │
        │ PostgreSQL  │ │ PostgreSQL │ │ MongoDB  │ │  PostgreSQL │
        └──────┬──────┘ └─────┬──────┘ └────┬─────┘ └──────┬────┘
               │              │             │              │
               └──────────────┴─────────────┴──────────────┘
                              │
                    ┌─────────▼──────────┐
                    │     RABBITMQ       │
                    │    Port: 5672      │
                    │  Mgmt UI: 15672    │
                    └────────────────────┘
```

## 🏗️ Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| API Gateway | 8080 | Redis | Entry point, routing, rate limiting |
| Customer Service | 8081 | PostgreSQL | B2B customer management |
| Vehicle Service | 8082 | PostgreSQL | Fleet vehicle management |
| Document Service | 8083 | MongoDB + MinIO | Document storage & management |
| Payment Service | 8084 | PostgreSQL | Invoicing & subscriptions |

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+

### Run with Docker Compose

```bash
# Clone the repository
cd fleet-management

# Start all services
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f api-gateway
```

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | - |
| Customer Swagger | http://localhost:8081/swagger-ui.html | - |
| Vehicle Swagger | http://localhost:8082/swagger-ui.html | - |
| Document Swagger | http://localhost:8083/swagger-ui.html | - |
| Payment Swagger | http://localhost:8084/swagger-ui.html | - |
| RabbitMQ UI | http://localhost:15672 | fleet_user / fleet_password |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |

## 📋 API Endpoints

### Customer Service (`/api/customers`)
```
POST   /api/customers              # Register new customer (PENDING)
GET    /api/customers/{id}         # Get customer by ID
GET    /api/customers              # List all customers (paginated)
GET    /api/customers/search       # Search customers
GET    /api/customers/pending      # List pending customers
PUT    /api/customers/{id}         # Update customer
PATCH  /api/customers/{id}/validate # Validate customer (ADMIN)
PATCH  /api/customers/{id}/status  # Update status (ADMIN)
DELETE /api/customers/{id}         # Soft delete (ADMIN)
```

### Vehicle Service (`/api/vehicles`)
```
POST   /api/vehicles               # Create vehicle
GET    /api/vehicles/{id}          # Get vehicle by ID
GET    /api/vehicles               # List all vehicles
GET    /api/vehicles/by-customer/{customerId}
GET    /api/vehicles/status/{status}
GET    /api/vehicles/expiring-insurance?days=30
GET    /api/vehicles/maintenance-due
PUT    /api/vehicles/{id}          # Update vehicle
PATCH  /api/vehicles/{id}/status   # Update status
PATCH  /api/vehicles/{id}/assign   # Assign to customer
PATCH  /api/vehicles/{id}/unassign # Unassign from customer
DELETE /api/vehicles/{id}          # Delete vehicle
```

### Document Service (`/api/documents`)
```
POST   /api/documents/upload       # Upload document (multipart)
GET    /api/documents/{id}         # Get document metadata
GET    /api/documents/{id}/download # Download file
GET    /api/documents              # Search documents
GET    /api/documents/expiring     # Get expiring documents
DELETE /api/documents/{id}         # Soft delete
```

### Payment Service (`/api/invoices`, `/api/subscriptions`, `/api/transactions`)
```
# Invoices
POST   /api/invoices               # Create invoice
GET    /api/invoices/{id}          # Get invoice
GET    /api/invoices               # List all invoices
GET    /api/invoices/customer/{customerId}
GET    /api/invoices/overdue       # List overdue invoices
POST   /api/invoices/{id}/pay      # Pay invoice (simulation)
POST   /api/invoices/{id}/cancel   # Cancel invoice

# Subscriptions
GET    /api/subscriptions          # List all subscriptions
GET    /api/subscriptions/{id}     # Get subscription
POST   /api/subscriptions          # Create subscription
PATCH  /api/subscriptions/{id}/upgrade
PATCH  /api/subscriptions/{id}/cancel

# Transactions
GET    /api/transactions?invoiceId=...
```

## 🔗 Communication Patterns

### Synchronous (REST/WebClient)
- `vehicle-service` → `customer-service`: Verify customer exists before assignment
- `payment-service` → `customer-service`: Verify customer before invoicing
- `payment-service` → `vehicle-service`: Verify vehicle before invoicing

### Asynchronous (RabbitMQ Events)

**Customer Events:**
- `CustomerCreatedEvent` → Payment creates BASIC subscription, Document creates folder
- `CustomerValidatedEvent` → Payment activates subscription
- `CustomerSuspendedEvent` → Vehicle deactivates vehicles, Payment suspends billing
- `CustomerDeletedEvent` → Vehicle orphans vehicles, Payment cancels subscriptions, Document archives documents
- `CustomerReactivatedEvent` → Vehicle reactivates vehicles

**Vehicle Events:**
- `VehicleCreatedEvent` → Document creates folder
- `VehicleAssignedEvent` → Payment creates activation invoice (50€)
- `VehicleStatusChangedEvent` → Document notified for MAINTENANCE

**Payment Events:**
- `InvoiceCreatedEvent` → Document stores PDF
- `InvoicePaidEvent` → Document marks as PAID, Customer reactivates if SUSPENDED
- `InvoiceOverdueEvent` → Customer suspends client
- `SubscriptionExpiredEvent` → Customer sets INACTIVE

## 👤 User Roles

| Role | Permissions |
|------|-------------|
| **SUPER_ADMIN** | Full platform access, manage admins |
| **ADMIN** | Validate customers, view all fleets, manage disputes |
| **CUSTOMER_ADMIN** | Manage own fleet, invoices, documents |
| **CUSTOMER_USER** | View vehicles, upload documents |

## 🔄 Business Flows

### 1. New Company Registration
```
1. POST /api/customers (self-registration)
   → Customer created with PENDING status
   → CustomerCreatedEvent published
   → Payment creates BASIC subscription (30 days free)
   → Document creates empty folder

2. ADMIN validates via PATCH /api/customers/{id}/validate
   → Status → ACTIVE
   → CustomerValidatedEvent published
   → Payment activates subscription
```

### 2. Add Vehicle to Fleet
```
1. POST /api/vehicles (with customerId)
   → Vehicle-service verifies customer via WebClient
   → Vehicle created
   → VehicleCreatedEvent published
   → Document creates vehicle folder
   → VehicleAssignedEvent published
   → Payment creates activation invoice (50€)
```

### 3. Pay Invoice
```
1. POST /api/invoices/{id}/pay
   → Invoice marked as PAID
   → InvoicePaidEvent published
   → Document marks invoice as PAID
   → If customer was SUSPENDED → reactivated
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific service tests
cd customer-service && mvn test

# Integration tests with Docker
docker-compose -f docker-compose.yml -f docker-compose.test.yml up
```

## 📊 Monitoring

- **Health Checks**: `/actuator/health` on each service
- **Metrics**: `/actuator/metrics` 
- **RabbitMQ UI**: http://localhost:15672
- **MinIO Console**: http://localhost:9001

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.3**
- **Spring Cloud Gateway**
- **Spring Data JPA / MongoDB**
- **Spring AMQP (RabbitMQ)**
- **PostgreSQL 16**
- **MongoDB 7**
- **MinIO (S3-compatible)**
- **Redis**
- **MapStruct**
- **Resilience4j**
- **Flyway**
- **OpenAPI/Swagger**

## 📁 Project Structure

```
fleet-management/
├── fleet-commons/          # Shared library (events, DTOs, exceptions)
├── api-gateway/            # Spring Cloud Gateway
├── customer-service/       # Customer management
├── vehicle-service/        # Vehicle management
├── document-service/       # Document storage
├── payment-service/        # Invoicing & subscriptions
├── docker-compose.yml      # Infrastructure + services
└── README.md
```

## 📝 License

MIT License - Fleet Management System


## 🪟 Windows Setup

### Prerequisites
1. **Docker Desktop** - [Download](https://docs.docker.com/desktop/install/windows-install/)
   - Enable WSL2 backend during installation
   - Start Docker Desktop and wait for it to be ready

2. **Java 21** - [Download Eclipse Temurin](https://adoptium.net/)
   - Verify: `java -version`

3. **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
   - Add to PATH, verify: `mvn -version`

### Quick Start (PowerShell)
```powershell
# 1. Navigate to project folder
cd D:leet_management1

# 2. Run the Windows start script
.\start-windows.ps1

# Or manually:
# Build shared library first
cd fleet-commons
mvn clean install -DskipTests
cd ..

# Start infrastructure
docker-compose up -d redis rabbitmq postgres-customer postgres-vehicle postgres-payment mongodb-document minio

# Wait 30 seconds, then start services
docker-compose up -d customer-service vehicle-service document-service payment-service api-gateway
```

### Troubleshooting

**Error: "Docker is not running"**
→ Start Docker Desktop from Start Menu, wait for the whale icon to stop animating.

**Error: "unable to get image... pipe dockerDesktopLinuxEngine"**
→ Docker Desktop n'est pas démarré ou utilise le mauvais backend.
   1. Ouvre Docker Desktop
   2. Settings → General → Use the WSL 2 based engine ✅
   3. Redémarre Docker Desktop

**Error: "version is obsolete"**
→ C'est juste un warning, ignore-le. Ou supprime la ligne `version: '3.8'` du docker-compose.yml.

**Port already in use**
→ Change les ports dans docker-compose.yml:
```yaml
ports:
  - "8081:8081"  # Change to "8091:8081" etc.
```

**Build fails with "fleet-commons not found"**
→ Tu dois d'abord builder fleet-commons localement:
```powershell
cd fleet-commons
mvn clean install -DskipTests
```

### Useful Commands
```powershell
# View all running containers
docker ps

# View logs for a service
docker-compose logs -f customer-service

# Restart a service
docker-compose restart vehicle-service

# Enter a container
docker exec -it fleet-customer-service sh

# Check RabbitMQ queues
docker exec -it fleet-rabbitmq rabbitmqctl list_queues

# Database access
docker exec -it fleet-postgres-customer psql -U fleet_user -d fleet_customer
```
