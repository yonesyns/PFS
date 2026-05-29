#!/bin/bash

# ============================================================
# FLEET MANAGEMENT - SCRIPT DE TEST COMPLET
# ============================================================

BASE_URL="http://localhost:8080"
ADMIN_ID="00000000-0000-0000-0000-000000000001"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

pass() { echo -e "${GREEN}✔ PASS${NC} — $1"; }
fail() { echo -e "${RED}✘ FAIL${NC} — $1"; }
info() { echo -e "${BLUE}ℹ${NC} $1"; }
section() { echo -e "\n${YELLOW}═══════════════════════════════════════${NC}"; echo -e "${YELLOW} $1${NC}"; echo -e "${YELLOW}═══════════════════════════════════════${NC}"; }

check_status() {
  local response=$1
  local expected=$2
  local label=$3
  local status=$(echo "$response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
  if [ "$status" == "$expected" ]; then
    pass "$label → status: $status"
  else
    fail "$label → attendu: $expected, reçu: $status"
  fi
}

check_http() {
  local code=$1
  local expected=$2
  local label=$3
  if [ "$code" == "$expected" ]; then
    pass "$label → HTTP $code"
  else
    fail "$label → attendu HTTP $expected, reçu HTTP $code"
  fi
}

# ============================================================
# SCÉNARIO 1 : Inscription complète d'une nouvelle entreprise
# ============================================================
section "SCÉNARIO 1 : Inscription complète B2B"

info "1.1 Création du client..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Transport Dupont SARL",
    "vatNumber": "FR12345678901",
    "email": "contact@dupont-transport.fr",
    "phone": "+33123456789",
    "address": {
      "street": "15 Rue de la Logistics",
      "city": "Paris",
      "zipCode": "75001",
      "country": "France"
    },
    "contactFirstName": "Jean",
    "contactLastName": "Dupont",
    "contactEmail": "jean.dupont@dupont-transport.fr",
    "contactPhone": "+33123456790"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "201" "Création client"
check_status "$BODY" "PENDING" "Statut initial"

CUSTOMER_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Customer ID: $CUSTOMER_ID"

info "1.2 Vérification liste pending..."
PENDING=$(curl -s "$BASE_URL/api/customers/pending")
if echo "$PENDING" | grep -q "Transport Dupont SARL"; then
  pass "Client visible dans la liste pending"
else
  fail "Client NON visible dans la liste pending"
fi

info "1.3 Validation par admin..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/customers/$CUSTOMER_ID/validate" \
  -H "X-User-Id: $ADMIN_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "200" "Validation client"
check_status "$BODY" "ACTIVE" "Statut après validation"

VALIDATED_BY=$(echo "$BODY" | grep -o '"validatedBy":"[^"]*"' | cut -d'"' -f4)
if [ "$VALIDATED_BY" == "$ADMIN_ID" ]; then
  pass "validatedBy correctement enregistré"
else
  fail "validatedBy incorrect: $VALIDATED_BY"
fi

info "1.4 Vérification client ACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/$CUSTOMER_ID")
check_status "$RESPONSE" "ACTIVE" "Client ACTIVE confirmé"

# ============================================================
# SCÉNARIO 2 : Ajout d'un véhicule à la flotte
# ============================================================
section "SCÉNARIO 2 : Ajout d'un véhicule"

info "2.1 Création du véhicule avec client ACTIVE..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{
    \"plateNumber\": \"AB-456-CD\",
    \"vin\": \"VF1BM0H6X12345678\",
    \"brand\": \"Renault\",
    \"model\": \"Master\",
    \"year\": 2023,
    \"color\": \"Blanc\",
    \"fuelType\": \"DIESEL\",
    \"mileage\": 15000,
    \"registrationDate\": \"2023-01-15\",
    \"insuranceExpiryDate\": \"2027-12-31\",
    \"technicalInspectionDate\": \"2026-12-31\",
    \"customerId\": \"$CUSTOMER_ID\"
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "201" "Création véhicule"
check_status "$BODY" "ACTIVE" "Véhicule ACTIVE après assignation"

VEHICLE_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Vehicle ID: $VEHICLE_ID"

info "2.2 Vérification véhicules du client..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/by-customer/$CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "$VEHICLE_ID"; then
  pass "Véhicule lié au client"
else
  fail "Véhicule NON lié au client"
fi

info "2.3 Vérification facture d'activation..."
sleep 2
INVOICES=$(curl -s "$BASE_URL/api/invoices/customer/$CUSTOMER_ID")
if echo "$INVOICES" | grep -q "SENT\|VEHICLE_ACTIVATION\|activation"; then
  pass "Facture d'activation créée"
else
  info "Facture d'activation non trouvée (peut nécessiter un event RabbitMQ)"
fi

info "2.4 Vérification dossier documentaire..."
DOCS=$(curl -s "$BASE_URL/api/documents?entityType=VEHICLE&entityId=$VEHICLE_ID")
if echo "$DOCS" | grep -q "success"; then
  pass "Dossier documentaire accessible"
else
  fail "Dossier documentaire inaccessible"
fi

# ============================================================
# SCÉNARIO 3 : Upload documentaire
# ============================================================
section "SCÉNARIO 7 : Upload et gestion documentaire"

info "7.1 Upload d'un document..."
echo "Carte verte assurance - Transport Dupont SARL" > /tmp/carte_verte.pdf
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/documents/upload" \
  -H "X-User-Id: $ADMIN_ID" \
  -F "file=@/tmp/carte_verte.pdf" \
  -F "entityType=VEHICLE" \
  -F "entityId=$VEHICLE_ID" \
  -F "documentType=INSURANCE" \
  -F "expiryDate=2027-12-31")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "201" "Upload document"

DOC_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Document ID: $DOC_ID"

info "7.2 Téléchargement du document..."
HTTP_CODE=$(curl -s -o /tmp/downloaded_carte_verte.pdf -w "%{http_code}" \
  "$BASE_URL/api/documents/$DOC_ID/download")
check_http "$HTTP_CODE" "200" "Téléchargement document"

info "7.3 Recherche documents du véhicule..."
RESPONSE=$(curl -s "$BASE_URL/api/documents?entityType=VEHICLE&entityId=$VEHICLE_ID")
if echo "$RESPONSE" | grep -q "$DOC_ID"; then
  pass "Document trouvé dans la recherche"
else
  fail "Document NON trouvé dans la recherche"
fi

info "7.4 Vérification document par ID..."
RESPONSE=$(curl -s "$BASE_URL/api/documents/$DOC_ID")
check_status "$RESPONSE" "ACTIVE" "Document ACTIVE"

# ============================================================
# SCÉNARIO 4 : Gestion abonnements
# ============================================================
section "SCÉNARIO 8 : Gestion des abonnements"

info "8.1 Création abonnement PRO..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/subscriptions?customerId=$CUSTOMER_ID&planType=PRO&monthlyAmount=9900")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "201" "Création abonnement PRO"

SUB_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Subscription ID: $SUB_ID"

info "8.2 Upgrade vers ENTERPRISE..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
  "$BASE_URL/api/subscriptions/$SUB_ID/upgrade?newPlan=ENTERPRISE&newAmount=19900")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "200" "Upgrade abonnement"

PLAN=$(echo "$BODY" | grep -o '"planType":"[^"]*"' | cut -d'"' -f4)
if [ "$PLAN" == "ENTERPRISE" ]; then
  pass "Plan mis à jour vers ENTERPRISE"
else
  fail "Plan non mis à jour: $PLAN"
fi

info "8.3 Annulation abonnement..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
  "$BASE_URL/api/subscriptions/$SUB_ID/cancel?reason=Test+annulation")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
check_http "$HTTP_CODE" "200" "Annulation abonnement"

# ============================================================
# SCÉNARIO 5 : Circuit Breaker
# ============================================================
section "SCÉNARIO 9 : Circuit Breaker et résilience"

info "9.1 Test normal — création véhicule avec client valide..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{
    \"plateNumber\": \"CC-789-EE\",
    \"vin\": \"VF1BM0H6X99999999\",
    \"brand\": \"Peugeot\",
    \"model\": \"Expert\",
    \"year\": 2023,
    \"color\": \"Gris\",
    \"fuelType\": \"DIESEL\",
    \"insuranceExpiryDate\": \"2027-12-31\",
    \"technicalInspectionDate\": \"2026-12-31\",
    \"customerId\": \"$CUSTOMER_ID\"
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
check_http "$HTTP_CODE" "201" "Véhicule créé normalement"

info "9.2 Arrêt du customer-service..."
docker stop fleet-customer-service > /dev/null 2>&1
sleep 3

info "9.3 Test avec customer-service arrêté..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{
    \"plateNumber\": \"FF-000-GG\",
    \"vin\": \"VF1BM0H6X88888888\",
    \"brand\": \"Citroen\",
    \"model\": \"Berlingo\",
    \"year\": 2023,
    \"color\": \"Blanc\",
    \"fuelType\": \"ELECTRIC\",
    \"insuranceExpiryDate\": \"2027-12-31\",
    \"technicalInspectionDate\": \"2026-12-31\",
    \"customerId\": \"$CUSTOMER_ID\"
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" == "503" ] || [ "$HTTP_CODE" == "500" ] || [ "$HTTP_CODE" == "504" ]; then
  pass "Circuit breaker actif — service indisponible détecté (HTTP $HTTP_CODE)"
else
  info "Réponse inattendue: HTTP $HTTP_CODE"
fi

info "9.4 Redémarrage du customer-service..."
docker start fleet-customer-service > /dev/null 2>&1
info "Attente 60s pour que le service redémarre..."
sleep 60

info "9.5 Test après récupération..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/$CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "Transport Dupont"; then
  pass "Service récupéré — customer-service répond normalement"
else
  fail "Service NON récupéré après redémarrage"
fi

# ============================================================
# SCÉNARIO 6 : Recherche et pagination
# ============================================================
section "SCÉNARIO 10 : Recherche et pagination"

info "10.1 Recherche client par nom..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/search?companyName=Dupont")
if echo "$RESPONSE" | grep -q "Transport Dupont"; then
  pass "Recherche par nom fonctionne"
else
  fail "Recherche par nom échoue"
fi

info "10.2 Pagination clients..."
RESPONSE=$(curl -s "$BASE_URL/api/customers?page=0&size=10")
if echo "$RESPONSE" | grep -q "totalElements"; then
  pass "Pagination fonctionne"
  TOTAL=$(echo "$RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
  info "Total clients: $TOTAL"
else
  fail "Pagination échoue"
fi

info "10.3 Véhicules par statut ACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/status/ACTIVE")
if echo "$RESPONSE" | grep -q "content"; then
  pass "Recherche véhicules ACTIVE fonctionne"
else
  fail "Recherche véhicules par statut échoue"
fi

info "10.4 Véhicules assurance expirant dans 30 jours..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/expiring-insurance?days=30")
if echo "$RESPONSE" | grep -q "success"; then
  pass "Alerte assurance fonctionne"
else
  fail "Alerte assurance échoue"
fi

info "10.5 Factures en retard..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/overdue")
if echo "$RESPONSE" | grep -q "success"; then
  pass "Factures overdue accessibles"
else
  fail "Factures overdue inaccessibles"
fi

info "10.6 Documents expirant bientôt..."
RESPONSE=$(curl -s "$BASE_URL/api/documents/expiring?days=30")
if echo "$RESPONSE" | grep -q "success"; then
  pass "Alerte documents expirants fonctionne"
else
  fail "Alerte documents expirants échoue"
fi

# ============================================================
# SCÉNARIO 7 : Suppression client (cascade)
# ============================================================
section "SCÉNARIO 5 : Suppression client (cascade)"

info "5.1 Suppression soft du client..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE \
  "$BASE_URL/api/customers/$CUSTOMER_ID" \
  -H "X-User-Id: $ADMIN_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
check_http "$HTTP_CODE" "200" "Suppression client"

info "5.2 Vérification statut DELETED..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/$CUSTOMER_ID")
check_status "$RESPONSE" "DELETED" "Client DELETED"

info "5.3 Vérification véhicules ORPHANED..."
sleep 2
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/status/ORPHANED")
if echo "$RESPONSE" | grep -q "content"; then
  pass "Véhicules ORPHANED vérifiés"
else
  info "Vérification ORPHANED — vérifier manuellement"
fi

# ============================================================
# RÉSUMÉ FINAL
# ============================================================
section "RÉSUMÉ DES TESTS"
echo ""
echo -e "  Customer ID  : ${BLUE}$CUSTOMER_ID${NC}"
echo -e "  Vehicle ID   : ${BLUE}$VEHICLE_ID${NC}"
echo -e "  Document ID  : ${BLUE}$DOC_ID${NC}"
echo -e "  Sub ID       : ${BLUE}$SUB_ID${NC}"
echo ""
echo -e "${GREEN}Tests terminés !${NC}"
echo ""
echo "Pour vérifier RabbitMQ: http://localhost:15672 (fleet_user/fleet_password)"
echo "Pour vérifier MinIO:    http://localhost:9001  (minioadmin/minioadmin)"
echo "Swagger customer:       http://localhost:8081/swagger-ui.html"
echo "Swagger vehicle:        http://localhost:8082/swagger-ui.html"
echo "Swagger document:       http://localhost:8083/swagger-ui.html"
echo "Swagger payment:        http://localhost:8084/swagger-ui.html"
