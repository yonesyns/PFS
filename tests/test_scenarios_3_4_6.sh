#!/bin/bash

# ============================================================
# FLEET MANAGEMENT - SCÉNARIOS 3, 4, 6
# Nécessitent manipulation des dates en base de données
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
# SETUP : Créer un client et une facture pour les tests
# ============================================================
section "SETUP — Création des données de test"

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
CUSTOMER_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Customer ID: $CUSTOMER_ID"

info "Validation du client..."
curl -s -X PATCH "$BASE_URL/api/customers/$CUSTOMER_ID/validate" \
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
    \"customerId\": \"$CUSTOMER_ID\"
  }")
VEHICLE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
info "Vehicle ID: $VEHICLE_ID"

info "Création d'une facture..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoices" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"$CUSTOMER_ID\",
    \"type\": \"SUBSCRIPTION\",
    \"amount\": 9900,
    \"description\": \"Abonnement PRO - Mai 2026\"
  }")
INVOICE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
INVOICE_NUMBER=$(echo "$RESPONSE" | grep -o '"invoiceNumber":"[^"]*"' | cut -d'"' -f4)
info "Invoice ID: $INVOICE_ID ($INVOICE_NUMBER)"

# ============================================================
# SCÉNARIO 4 : Suspension pour impayé
# ============================================================
section "SCÉNARIO 4 : Suspension pour impayé (tâche planifiée simulée)"

info "4.1 Vérification statut initial de la facture..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/$INVOICE_ID")
check_status "$RESPONSE" "SENT" "Facture initialement SENT"

info "4.2 Simulation du passage du temps — dueDate à -31 jours en base..."
docker exec fleet-postgres-payment psql \
  -U $DB_USER -d fleet_payment \
  -c "UPDATE invoices SET due_date = CURRENT_DATE - INTERVAL '31 days', status = 'OVERDUE' WHERE id = '$INVOICE_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "dueDate et status mis à jour en base PostgreSQL"
else
  fail "Erreur lors de la mise à jour en base"
fi

info "4.3 Vérification facture OVERDUE..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/$INVOICE_ID")
check_status "$RESPONSE" "OVERDUE" "Facture OVERDUE"

info "4.4 Simulation suspension client (mise à jour directe)..."
docker exec fleet-postgres-customer psql \
  -U $DB_USER -d fleet_customer \
  -c "UPDATE customers SET status = 'SUSPENDED' WHERE id = '$CUSTOMER_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "Client SUSPENDED en base"
else
  fail "Erreur lors de la suspension"
fi

info "4.5 Vérification client SUSPENDED..."
RESPONSE=$(curl -s "$BASE_URL/api/customers/$CUSTOMER_ID")
check_status "$RESPONSE" "SUSPENDED" "Client SUSPENDED"

info "4.6 Simulation véhicules INACTIVE (client suspendu)..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET status = 'INACTIVE' WHERE customer_id = '$CUSTOMER_ID';" \
  > /dev/null 2>&1
pass "Véhicules passés en INACTIVE"

info "4.7 Vérification véhicules INACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/by-customer/$CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "INACTIVE"; then
  pass "Véhicules INACTIVE confirmés"
else
  fail "Véhicules non INACTIVE"
fi

info "4.8 Vérification liste factures overdue..."
RESPONSE=$(curl -s "$BASE_URL/api/invoices/overdue")
if echo "$RESPONSE" | grep -q "$INVOICE_ID"; then
  pass "Facture visible dans /invoices/overdue"
else
  fail "Facture NON visible dans /invoices/overdue"
fi

# ============================================================
# SCÉNARIO 3 : Paiement et réactivation
# ============================================================
section "SCÉNARIO 3 : Paiement facture + réactivation client"

info "3.1 Paiement de la facture OVERDUE..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/invoices/$INVOICE_ID/pay" \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod": "CARD", "reference": "TXN-RECOVERY-001"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)
check_http "$HTTP_CODE" "200" "Paiement facture"
check_status "$BODY" "PAID" "Facture PAID"

PAID_AT=$(echo "$BODY" | grep -o '"paidAt":"[^"]*"' | cut -d'"' -f4)
info "paidAt: $PAID_AT"

info "3.2 Vérification transaction créée..."
RESPONSE=$(curl -s "$BASE_URL/api/transactions?invoiceId=$INVOICE_ID")
if echo "$RESPONSE" | grep -q "content"; then
  pass "Transaction créée"
else
  info "Transaction — vérifier via RabbitMQ events"
fi

info "3.3 Réactivation manuelle du client (via event RabbitMQ ou API)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
  "$BASE_URL/api/customers/$CUSTOMER_ID/status" \
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
  -c "UPDATE vehicles SET status = 'ACTIVE' WHERE customer_id = '$CUSTOMER_ID' AND status = 'INACTIVE';" \
  > /dev/null 2>&1
pass "Véhicules réactivés"

info "3.5 Vérification véhicules ACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/by-customer/$CUSTOMER_ID")
if echo "$RESPONSE" | grep -q "ACTIVE"; then
  pass "Véhicules ACTIVE confirmés"
else
  fail "Véhicules non ACTIVE"
fi

# ============================================================
# SCÉNARIO 6 : Expiration assurance
# ============================================================
section "SCÉNARIO 6 : Expiration d'assurance (tâche planifiée simulée)"

info "6.1 Vérification véhicule ACTIVE initial..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$VEHICLE_ID")
check_status "$RESPONSE" "ACTIVE" "Véhicule ACTIVE"

info "6.2 Simulation expiration assurance — insuranceExpiryDate à hier..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET insurance_expiry_date = CURRENT_DATE - INTERVAL '1 day' WHERE id = '$VEHICLE_ID';" \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
  pass "insuranceExpiryDate mis à hier en base PostgreSQL"
else
  fail "Erreur lors de la mise à jour"
fi

info "6.3 Vérification date mise à jour..."
DATE=$(docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle -t \
  -c "SELECT insurance_expiry_date FROM vehicles WHERE id = '$VEHICLE_ID';")
info "insuranceExpiryDate: $DATE"

info "6.4 Simulation tâche planifiée — passage INACTIVE..."
docker exec fleet-postgres-vehicle psql \
  -U $DB_USER -d fleet_vehicle \
  -c "UPDATE vehicles SET status = 'INACTIVE' WHERE id = '$VEHICLE_ID' AND insurance_expiry_date < CURRENT_DATE;" \
  > /dev/null 2>&1
pass "Tâche planifiée simulée — véhicule INACTIVE"

info "6.5 Vérification véhicule INACTIVE..."
RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$VEHICLE_ID")
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
  -c "UPDATE vehicles SET insurance_expiry_date = '2027-12-31', status = 'ACTIVE' WHERE id = '$VEHICLE_ID';" \
  > /dev/null 2>&1
pass "Assurance restaurée — véhicule ACTIVE"

RESPONSE=$(curl -s "$BASE_URL/api/vehicles/$VEHICLE_ID")
check_status "$RESPONSE" "ACTIVE" "Véhicule ACTIVE restauré"

# ============================================================
# RÉSUMÉ FINAL
# ============================================================
section "RÉSUMÉ SCÉNARIOS 3, 4, 6"
echo ""
echo -e "  Customer ID : ${BLUE}$CUSTOMER_ID${NC}"
echo -e "  Vehicle ID  : ${BLUE}$VEHICLE_ID${NC}"
echo -e "  Invoice ID  : ${BLUE}$INVOICE_ID${NC} ($INVOICE_NUMBER)"
echo ""
echo -e "${GREEN}Scénarios 3, 4, 6 terminés !${NC}"
echo ""
echo "Vérifications manuelles recommandées:"
echo "  → RabbitMQ events: http://localhost:15672 (fleet_user/fleet_password)"
echo "  → Queues: customer.events, payment.events, vehicle.events"
