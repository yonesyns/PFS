# 🧪 Tests Bruno - Fleet Management API

## 📋 **Prérequis**

### Installation Bruno:
```bash
# Via npm
npm install -g @usebruno/cli

# Ou télécharger depuis: https://www.usebruno.com/
```

### Démarrage des services:
```bash
# Build et démarrage
./build-all.bat
docker-compose up -d

# Vérifier que tous les services sont UP
curl http://localhost:8761  # Eureka
curl http://localhost:8080/actuator/health  # API Gateway
```

## 🗂️ **Structure des tests**

```
bruno-tests/
├── bruno.json                    # Configuration Bruno
├── environments/
│   └── Local.bru                # Variables d'environnement
├── 01 - Health Checks/          # Tests de santé
├── 02 - Customer Service/       # Tests clients
├── 03 - Vehicle Service/        # Tests véhicules
├── 04 - Payment Service/        # Tests paiements
├── 05 - Document Service/       # Tests documents
├── 06 - Integration Tests/      # Tests d'intégration
└── test-document.txt           # Fichier test pour upload
```

## 🚀 **Exécution des tests**

### Via Bruno GUI:
1. Ouvrir Bruno
2. Ouvrir la collection: `d:\PFS\bruno-tests`
3. Sélectionner l'environnement "Local"
4. Exécuter les tests dans l'ordre

### Via CLI:
```bash
cd d:\PFS\bruno-tests

# Exécuter tous les tests
bru run --env Local

# Exécuter un dossier spécifique
bru run "01 - Health Checks" --env Local

# Exécuter un test spécifique
bru run "02 - Customer Service/Create Customer.bru" --env Local

# Avec rapport détaillé
bru run --env Local --reporter json --output results.json
```

## 📊 **Ordre d'exécution recommandé**

### 1. **Health Checks** (Vérification des services)
- ✅ Eureka Server Health
- ✅ API Gateway Health  
- ✅ Customer Service Health

### 2. **Customer Service** (Gestion clients)
- ✅ Create Customer → Stocke `testCustomerId`
- ✅ Get All Customers
- ✅ Get Customer by ID

### 3. **Vehicle Service** (Gestion véhicules)
- ✅ Create Vehicle → Stocke `testVehicleId`
- ✅ Get Available Vehicles

### 4. **Payment Service** (Traitement paiements)
- ✅ Process Payment → Utilise `testCustomerId`
- ✅ Get Payments by Customer

### 5. **Document Service** (Gestion documents)
- ✅ Upload Document → Utilise `testCustomerId`
- ✅ Get Documents by Entity

### 6. **Integration Tests** (Tests bout-en-bout)
- ✅ Complete Booking Workflow
- ✅ Performance Test

## 🔧 **Variables d'environnement**

```javascript
// URLs des services
baseUrl: http://localhost:8080           // API Gateway
customerServiceUrl: http://localhost:8081
vehicleServiceUrl: http://localhost:8082
paymentServiceUrl: http://localhost:8083
documentServiceUrl: http://localhost:8084

// IDs de test (auto-générés)
testCustomerId: 1
testVehicleId: 1
testPaymentId: 1
testDocumentId: 1
```

## 📝 **Exemples de données de test**

### Customer:
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "status": "ACTIVE",
  "type": "INDIVIDUAL"
}
```

### Vehicle:
```json
{
  "vin": "1HGBH41JXMN109186",
  "make": "Toyota",
  "model": "Camry",
  "year": 2023,
  "color": "Blue",
  "licensePlate": "ABC-123",
  "type": "CAR",
  "mileage": 15000.5,
  "location": "New York Depot"
}
```

### Payment:
```json
{
  "customerId": 1,
  "bookingId": 1,
  "amount": 299.99,
  "method": "CREDIT_CARD",
  "description": "Vehicle rental payment"
}
```

## 🧪 **Types de tests inclus**

### ✅ **Tests fonctionnels:**
- CRUD operations pour tous les services
- Validation des données
- Gestion des erreurs

### ✅ **Tests d'intégration:**
- Workflow complet de réservation
- Communication inter-services
- Cohérence des données

### ✅ **Tests de performance:**
- Temps de réponse < 2 secondes
- Monitoring des métriques
- Tests de charge

### ✅ **Tests de santé:**
- Vérification des endpoints `/actuator/health`
- Status des services
- Connectivité réseau

## 🔍 **Assertions et validations**

```javascript
// Status codes
assert { res.status: eq 200 }
assert { res.status: eq 201 }

// Response body
assert { res.body.firstName: eq John }
assert { res.body.status: eq ACTIVE }

// Performance
assert { res.responseTime: lt 2000 }

// Custom tests
tests {
  test("Customer created successfully", function() {
    expect(res.getStatus()).to.equal(201);
    expect(res.getBody().firstName).to.equal("John");
  });
}
```

## 📈 **Monitoring et rapports**

### Métriques collectées:
- ✅ Temps de réponse par endpoint
- ✅ Taux de succès/échec
- ✅ Validation des données
- ✅ Performance des services

### Rapports disponibles:
```bash
# Rapport JSON détaillé
bru run --env Local --reporter json --output test-results.json

# Rapport HTML (si disponible)
bru run --env Local --reporter html --output test-report.html
```

## 🚨 **Troubleshooting**

### Services non disponibles:
```bash
# Vérifier les services
docker-compose ps
docker-compose logs api-gateway

# Redémarrer si nécessaire
docker-compose restart
```

### Tests échouent:
1. Vérifier l'ordre d'exécution
2. Vérifier les variables d'environnement
3. Vérifier les données de test
4. Consulter les logs des services

### Performance dégradée:
1. Vérifier les ressources système
2. Consulter Grafana: http://localhost:3000
3. Vérifier Prometheus: http://localhost:9090

## 🎯 **Commandes utiles**

```bash
# Tests rapides
bru run "01 - Health Checks" --env Local

# Tests complets avec rapport
bru run --env Local --reporter json | jq '.'

# Tests en boucle (stress test)
for i in {1..10}; do bru run "06 - Integration Tests/Performance Test.bru" --env Local; done
```

Les tests Bruno sont maintenant prêts pour valider complètement votre architecture microservices! 🚀