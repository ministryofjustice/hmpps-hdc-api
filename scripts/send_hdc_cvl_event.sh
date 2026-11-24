#!/usr/bin/env bash

set -euo pipefail

VALID_EVENT_TYPES=(
  OPT_IN
  OPT_OUT
  POSTPONE
  RESUME
  REFUSE
  ADDRESS_WITHDRAWN
  RISK_CHECKS_SUBMITTED
)

usage() {
  echo "Usage: $0 <event-type> <licence-id> <booking-id> <noms-number> <triggered-by> [reason]" >&2
  echo "  event-type: ${VALID_EVENT_TYPES[*]}" >&2
  echo "  reason defaults to 'test'" >&2
  echo "Example: $0 OPT_IN 123 456789 A1234AA TEST_USER" >&2
  exit 2
}

if [[ "$#" -lt 5 || "$#" -gt 6 ]]; then
  usage
fi

EVENT_TYPE="$1"
LICENCE_ID="$2"
BOOKING_ID="$3"
NOMS_NUMBER="$4"
TRIGGERED_BY="$5"
REASON="${6:-test}"

if [[ ! " ${VALID_EVENT_TYPES[*]} " =~ " $EVENT_TYPE " ]]; then
  echo "Invalid event type: $EVENT_TYPE" >&2
  usage
fi

for id in "$LICENCE_ID" "$BOOKING_ID"; do
  if [[ ! "$id" =~ ^[0-9]+$ ]]; then
    echo "Licence and booking IDs must be positive integers: $id" >&2
    exit 2
  fi
done

if [[ -z "$NOMS_NUMBER" || -z "$TRIGGERED_BY" ]]; then
  echo "nomsNumber and triggeredBy must be supplied" >&2
  exit 2
fi

ENVIRONMENT="dev"
BASE_URL="https://hdc-api-$ENVIRONMENT.hmpps.service.justice.gov.uk"
AUTH_URL="https://sign-in-$ENVIRONMENT.hmpps.service.justice.gov.uk/auth"
TOKEN_URL="$AUTH_URL/oauth/token"
NAMESPACE="licences-$ENVIRONMENT"
API_PATH="/licences/cvl-events"

REQUEST_JSON=$(jq -nc \
  --arg eventType "$EVENT_TYPE" \
  --argjson licenceId "$LICENCE_ID" \
  --argjson bookingId "$BOOKING_ID" \
  --arg nomsNumber "$NOMS_NUMBER" \
  --arg triggeredBy "$TRIGGERED_BY" \
  --arg reason "$REASON" \
  '{eventType: $eventType, licenceId: $licenceId, bookingId: $bookingId, nomsNumber: $nomsNumber, triggeredBy: $triggeredBy, reason: $reason}')

echo "Fetching API credentials from the $NAMESPACE namespace..."
SECRETS=$(kubectl -n "$NAMESPACE" get secret licences -o json)
echo "Extracting admin API client credentials..."
CLIENT_ID=$(jq -er '.data.ADMIN_API_CLIENT_ID | @base64d | select(length > 0)' <<< "$SECRETS")
CLIENT_SECRET=$(jq -er '.data.ADMIN_API_CLIENT_SECRET | @base64d | select(length > 0)' <<< "$SECRETS")
echo "Requesting access token from $TOKEN_URL..."
TOKEN=$(curl --fail-with-body --silent --show-error --request POST \
  --user "$CLIENT_ID:$CLIENT_SECRET" \
  --data 'grant_type=client_credentials' \
  "$TOKEN_URL" | jq -er '.access_token | select(type == "string" and length > 0)')

echo "Sending HDC CVL event: $REQUEST_JSON"
HTTP_STATUS=$(curl --silent --show-error --globoff \
  --output /dev/stderr --write-out '%{http_code}' \
  "$BASE_URL$API_PATH" \
  --request POST \
  --header "Authorization: Bearer $TOKEN" \
  --header "Accept: application/json" \
  --header "Content-Type: application/json" \
  --data "$REQUEST_JSON")

echo
echo "HTTP status: $HTTP_STATUS"
[[ "$HTTP_STATUS" == "200" ]]
