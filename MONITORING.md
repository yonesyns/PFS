# Monitoring Stack Documentation

## 🔍 Prometheus Configuration

### Métriques collectées:
- **Application metrics**: HTTP requests, response times, JVM memory
- **Redis metrics**: Connections, memory usage, commands/sec
- **Kafka metrics**: Messages, topics, consumer lag
- **Custom business metrics**: Vehicles created, payments processed

### Alertes configurées:
- Service down (> 1 minute)
- High response time (> 1 second)
- High memory usage (> 80%)
- High error rate (> 10%)
- Redis down

### Accès:
- URL: http://localhost:9090
- Targets: http://localhost:9090/targets
- Alerts: http://localhost:9090/alerts

## 📊 Grafana Dashboards

### Dashboard Principal: "Fleet Management Microservices"
- **Service Health Status**: État de tous les services
- **HTTP Request Rate**: Taux de requêtes par service
- **Response Time (95th percentile)**: Temps de réponse
- **JVM Memory Usage**: Utilisation mémoire Java
- **Redis Operations**: Opérations Redis
- **Kafka Messages**: Messages Kafka

### Dashboard Redis: "Redis Monitoring"
- **Connection Status**: État de connexion Redis
- **Connected Clients**: Nombre de clients connectés
- **Memory Usage**: Utilisation mémoire Redis
- **Commands Per Second**: Commandes par seconde
- **Cache Hit Rate**: Taux de succès du cache
- **Keys by Database**: Nombre de clés par DB

### Accès:
- URL: http://localhost:3000
- Login: admin/admin
- Dashboards auto-provisionnés

## 🔴 Redis Configuration

### Utilisation dans le projet:
1. **Cache**: Mise en cache des véhicules, clients
2. **Rate Limiting**: Limitation de taux API Gateway
3. **Session Storage**: Stockage des sessions utilisateur

### Configuration optimisée:
- **Max Memory**: 256MB avec politique LRU
- **Persistence**: Snapshots automatiques
- **Monitoring**: Métriques exposées via redis-exporter

### Commandes utiles:
```bash
# Connexion Redis
docker exec -it pfs-redis-1 redis-cli

# Voir les clés
KEYS *

# Statistiques
INFO stats

# Mémoire utilisée
INFO memory
```

### Accès:
- Host: localhost:6379
- Metrics: http://localhost:9121/metrics

## 🚀 Démarrage rapide

### Option 1: Démarrage complet
```bash
docker-compose up -d
```

### Option 2: Démarrage par étapes
```bash
# Infrastructure
start-monitoring.bat

# Ou manuellement:
docker-compose up -d redis prometheus grafana
docker-compose up -d zookeeper kafka zipkin
docker-compose up -d eureka-server config-server
docker-compose up -d api-gateway
docker-compose up -d customer-service vehicle-service payment-service document-service
```

## 📈 Métriques personnalisées

### Dans les services Spring Boot:
```java
@Component
public class CustomMetrics {
    private final Counter vehicleCreated = Counter.builder("vehicles_created_total")
            .description("Total vehicles created")
            .register(Metrics.globalRegistry);
    
    private final Timer paymentProcessingTime = Timer.builder("payment_processing_seconds")
            .description("Payment processing time")
            .register(Metrics.globalRegistry);
}
```

### Exposition automatique:
- Endpoint: `/actuator/prometheus`
- Format: Prometheus metrics format
- Collecte: Automatique par Prometheus

## 🔧 Configuration avancée

### Prometheus retention:
```yaml
command:
  - '--storage.tsdb.retention.time=30d'
  - '--storage.tsdb.retention.size=10GB'
```

### Grafana plugins:
```yaml
environment:
  - GF_INSTALL_PLUGINS=redis-datasource,grafana-piechart-panel
```

### Redis clustering (production):
```yaml
redis-cluster:
  image: redis:7-alpine
  command: redis-cli --cluster create --cluster-replicas 1
```

## 🚨 Troubleshooting

### Prometheus ne collecte pas les métriques:
1. Vérifier les endpoints `/actuator/prometheus`
2. Vérifier la configuration `prometheus.yml`
3. Vérifier les health checks

### Grafana dashboards vides:
1. Vérifier la datasource Prometheus
2. Vérifier les requêtes PromQL
3. Vérifier les labels des métriques

### Redis connexion failed:
1. Vérifier le service Redis: `docker ps`
2. Vérifier les logs: `docker logs pfs-redis-1`
3. Tester la connexion: `redis-cli ping`

## 📊 URLs importantes

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | admin/admin |
| Prometheus | http://localhost:9090 | - |
| Redis | localhost:6379 | - |
| API Gateway | http://localhost:8080 | - |
| Eureka | http://localhost:8761 | - |
| Zipkin | http://localhost:9411 | - |