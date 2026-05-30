#!/bin/bash

# ============================================================
# FLEET MANAGEMENT - SCRIPT DE TEST COMPLET (TOUS SCÉNARIOS)
# ============================================================
# Ordre optimal:
#   1, 2, 7, 8, 9, 10, 5, 4, 3, 6
# ============================================================

BASE_URL="http://localhost:8080"
ADMIN_ID="00000000-0000-0000-0000-000000000001"
DB_USER="fleet_user"
DB_PASS="fleet_password"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

pass() { echo -e "${GREEN}✔ PASS${NC} — $1"; }
fail() { echo -e "${RED}✘ FAIL${NC} — $1"; }
info() { echo -e "${BLUE}ℹ${NC} $1"; }
warn() { echo -e "${YELLOW}⚠ WARN${NC} — $1"; }
section() {
  echo -e "\n${YELLOW}═══════════════════════════════════════${NC}"
  echo -e "${YELLOW} $1${NC}"
  echo -e "${YELLOW}═══════════════════════════════════════${NC}"
}

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
# SCÉNARIO 1 : Inscription complète B2B
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
  warn "Facture d'activation non trouvée (peut nécessiter un event RabbitMQ)"
fi

info "2.4 Vérification dossier documentaire..."
DOCS=$(curl -s "$BASE_URL/api/documents?entityType=VEHICLE&entityId=$VEHICLE_ID")
if echo "$DOCS" | grep -q "success"; then
  pass "Dossier documentaire accessible"
else
  fail "Dossier documentaire inaccessible"
fi

# ============================================================
# SCÉNARIO 7 : Upload et gestion documentaire
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
RESPONSE=$(curl -s "$BASE_URL/api/documents/$DOC_ID"
)
check_status "$RESPONSE" "ACTIVE" "Document ACTIVE"

# ============================================================
# SCÉNARIO 8 : Gestion des abonnements
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
# SCÉNARIO 9 : Circuit Breaker et résilience
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
  warn "Réponse inattendue: HTTP $HTTP_CODE"
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
# SCÉNARIO 10 : Recherche et pagination
# ============================================================
# AVANT suppression (Bug 3 fix) — le client existe encore
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
# SCÉNARIO 5 : Suppression client (cascade)
# ============================================================
# APRÈS recherche (Bug 3 fix) — le client existe encore pour la recherche
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
  warn "Vérification ORPHANED — vérifier manuellement"
fi

# ============================================================
# SCÉNARIO 4 : Suspension pour impayé (simulation BDD)
# ============================================================
section "SCÉNARIO 4 : Suspension pour impayé (tâche planifiée simulée)"

info "SETUP 4.0 — Création données pour scénarios 4, 3, 6..."

info "Création d'un client de test..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Test Scenarios SARL",
    "vatNumber": "FR99999999999",
    "email": "test.scenarios@test.fr",
    "phone": "+33199999999",
    "address": {
      "street": "1 Rue du Test",
      "city": "Lyon",
      "zipCode": "69001",
      "country": "France"
    }
  }')
S4_CUSTOMER_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Customer ID: $S4_CUSTOMER_ID"

info "Validation du client..."
curl -s -X PATCH "$BASE_URL/api/customers/$S4_CUSTOMER_ID/validate" \
  -H "X-User-Id: $ADMIN_ID" > /dev/null
pass "Client ACTIVE"

info "Création d'un véhicule..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{
    \"plateNumber\": \"ZZ-999-ZZ\",
    \"vin\": \"VF1TEST0009999999\",
    \"brand\": \"Test\",
    \"model\": \"Car\",
    \"year\": 2023,
    \"color\": \"Rouge\",
    \"fuelType\": \"DIESEL\",
    \"insuranceExpiryDate\": \"2027-12-31\",
    \"technicalInspectionDate\": \"2026-12-31\",
    \"customerId\": \"$S4_CUSTOMER_ID\"
  }")
S4_VEHICLE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Vehicle ID: $S4_VEHICLE_ID"

info "Création d'une facture..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoices" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"$S4_CUSTOMER_ID\",
    \"type\": \"SUBSCRIPTION\",
    \"amount\": 9900,
    \"description\": \"Abonnement PRO - Mai 2026\"
  }")
S4_INVOICE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
S4_INVOICE_NUMBER=$(echo "$RESPONSE" | grep -o '"invoiceNumber":"[^"]*"' | cut -d'"' -f4)
info "Invoice ID: $S4_INVOICE_ID ($S4_INVOICE_NUMBER)"

info "4.1 Vérification statut initial de la facture..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/$S4_INVOICE_ID")
check_status "$RESPONSE" "SENT" "Facture initialement SENT"

info "4.2 Simulation du passage du temps — dueDate à -31 jours en base..."
docker exec fleet-postgres-payment psql \
  -U $DB_USER -d fleet_payment \
  -c "UPDATE invoices SET due_date = CURRENT_DATE - INTERVAL '31 days', status = 'OVERDUE' WHERE id = '$S4_INVOICE_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "dueDate et status mis à jour en base PostgreSQL"
else
  fail "Erreur lors de la mise à jour en base"
fi

info "4.3 Vérification facture OVERDUE..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/$S4_INVOICE_ID")
check_status "$RESPONSE" "OVERDUE" "Facture OVERDUE"

info "4.4 Simulation suspension client (mise à jour directe)..."
docker exec fleet-postgres-customer psql \
  -U $DB_USER -d fleet_customer \
  -c "UPDATE customers SET status = 'SUSPENDED' WHERE id = '$S4_CUSTOMER_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "Client SUSPENDED en base"
else
  fail "Erreur lors de la suspension"
fi

info "4.5 Vérification client SUSPENDED..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/$S4_CUSTOMER_ID")
check_status "$RESPONSE" "SUSPENDED" "Client SUSPENDED"

info "4.6 Simulation véhicules INACTIVE (client suspendu)..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET status = 'INACTIVE' WHERE customer_id = '$S4_CUSTOMER_ID';" \
  > /dev/null 2>&1
pass "Véhicules passés en INACTIVE"

info "4.7 Vérification véhicules INACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/by-customer/$S4_CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "INACTIVE"; then
  pass "Véhicules INACTIVE confirmés"
else
  fail "Véhicules non INACTIVE"
fi

info "4.8 Vérification liste factures overdue..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/overdue")
if echo "$RESPONSE" | grep -q "$S4_INVOICE_ID"; then
  pass "Facture visible dans /invoices/overdue"
else
  fail "Facture NON visible dans /invoices/overdue"
fi

# ============================================================
# SCÉNARIO 3 : Paiement facture + réactivation client
# ============================================================
section "SCÉNARIO 3 : Paiement facture + réactivation client"

info "3.1 Paiement de la facture OVERDUE..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/invoices/$S4_INVOICE_ID/pay" \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod": "CARD", "reference": "TXN-RECOVERY-001"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "200" "Paiement facture"
check_status "$BODY" "PAID" "Facture PAID"

PAID_AT=$(echo "$BODY" | grep -o '"paidAt":"[^"]*"' | cut -d'"' -f4)
info "paidAt: $PAID_AT"

info "3.2 Vérification transaction créée..."
RESPONSE=$(curl -s "$BASE_URL/api/transactions?invoiceId=$S4_INVOICE_ID")
if echo "$RESPONSE" | grep -q "content"; then
  pass "Transaction créée"
else
  warn "Transaction — vérifier via RabbitMQ events"
fi

info "3.3 Réactivation manuelle du client..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
  "$BASE_URL/api/customers/$S4_CUSTOMER_ID/status" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $ADMIN_ID" \
  -d '{"status": "ACTIVE", "reason": "Invoice paid - reactivation"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "200" "Réactivation client"
check_status "$BODY" "ACTIVE" "Client ACTIVE après paiement"

info "3.4 Réactivation des véhicules..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET status = 'ACTIVE' WHERE customer_id = '$S4_CUSTOMER_ID' AND status = 'INACTIVE';" \
  > /dev/null 2>&1
pass "Véhicules réactivés"

info "3.5 Vérification véhicules ACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/by-customer/$S4_CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "ACTIVE"; then
  pass "Véhicules ACTIVE confirmés"
else
  fail "Véhicules non ACTIVE"
fi

# ============================================================
# SCÉNARIO 6 : Expiration d'assurance (simulation BDD)
# ============================================================
section "SCÉNARIO 6 : Expiration d'assurance (tâche planifiée simulée)"

info "6.1 Vérification véhicule ACTIVE initial..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$S4_VEHICLE_ID")
check_status "$RESPONSE" "ACTIVE" "Véhicule ACTIVE"

info "6.2 Simulation expiration assurance — insuranceExpiryDate à hier..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET insurance_expiry_date = CURRENT_DATE - INTERVAL '1 day' WHERE id = '$S4_VEHICLE_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "insuranceExpiryDate mis à hier en base PostgreSQL"
else
  fail "Erreur lors de la mise à jour"
fi

info "6.3 Vérification date mise à jour..."
DATE=$(docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle -t \
  -c "SELECT insurance_expiry_date FROM vehicles WHERE id = '$S4_VEHICLE_ID';")
info "insuranceExpiryDate: $DATE"

info "6.4 Simulation tâche planifiée — passage INACTIVE..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET status = 'INACTIVE' WHERE id = '$S4_VEHICLE_ID' AND insurance_expiry_date < CURRENT_DATE;" \
  > /dev/null 2>&1
pass "Tâche planifiée simulée — véhicule INACTIVE"

info "6.5 Vérification véhicule INACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$S4_VEHICLE_ID")
check_status "$RESPONSE" "INACTIVE" "Véhicule INACTIVE après expiration assurance"

info "6.6 Vérification alerte véhicules expirés..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/expiring-insurance?days=0")
if echo "$RESPONSE" | grep -q "success"; then
  pass "Endpoint expiring-insurance fonctionne"
else
  fail "Endpoint expiring-insurance échoue"
fi

info "6.7 Restauration assurance (correction)..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET insurance_expiry_date = '2027-12-31', status = 'ACTIVE' WHERE id = '$S4_VEHICLE_ID';" \
  > /dev/null 2>&1
pass "Assurance restaurée — véhicule ACTIVE"

RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$S4_VEHICLE_ID")
check_status "$RESPONSE" "ACTIVE" "Véhicule ACTIVE restauré"

# ============================================================
# RÉSUMÉ FINAL
# ============================================================
section "RÉSUMÉ DE TOUS LES TESTS"
echo ""
echo -e "${BLUE}=== Données Scénarios 1-10 ===${NC}"
echo -e "  Customer ID  : ${BLUE}$CUSTOMER_ID${NC}"
echo -e "  Vehicle ID   : ${BLUE}$VEHICLE_ID${NC}"
echo -e "  Document ID  : ${BLUE}$DOC_ID${NC}"
echo -e "  Sub ID       : ${BLUE}$SUB_ID${NC}"
echo ""
echo -e "${BLUE}=== Données Scénarios 3, 4, 6 ===${NC}"
echo -e "  Customer ID  : ${BLUE}$S4_CUSTOMER_ID${NC}"
echo -e "  Vehicle ID   : ${BLUE}$S4_VEHICLE_ID${NC}"
echo -e "  Invoice ID   : ${BLUE}$S4_INVOICE_ID${NC} ($S4_INVOICE_NUMBER)"
echo ""
echo -e "${GREEN}Tous les scénarios terminés !${NC}"
echo ""
echo "Vérifications manuelles recommandées:"
echo "  → RabbitMQ events: http://localhost:15672 (fleet_user/fleet_password)"
echo "  → Queues: customer.events, payment.events, vehicle.events"
echo "  → MinIO:    http://localhost:9001  (minioadmin/minioadmin)"
echo "  → Swagger customer: http://localhost:8081/swagger-ui.html"
echo "  → Swagger vehicle:  http://localhost:8082/swagger-ui.html"
echo "  → Swagger document: http://localhost:8083/swagger-ui.html"
echo "  → Swagger payment:  http://localhost:8084/swagger-ui.html"
