#!/usr/bin/env bash

set -euo pipefail

if [[ "$#" -eq 0 ]]; then
  echo "Usage: $0 <booking-id> [booking-id ...]" >&2
  exit 2
fi

for booking_id in "$@"; do
  if [[ ! "$booking_id" =~ ^[0-9]+$ ]]; then
    echo "Booking IDs must be positive integers: $booking_id" >&2
    exit 2
  fi
done

ENVIRONMENT="dev"
BASE_URL=https://hdc-api-$ENVIRONMENT.hmpps.service.justice.gov.uk
AUTH_URL=https://sign-in-$ENVIRONMENT.hmpps.service.justice.gov.uk/auth
TOKEN_URL="$AUTH_URL/oauth/token"
NAMESPACE="licences-$ENVIRONMENT"
API_PATH="/licence/hdc/status"
BOOKING_IDS_JSON=$(printf '%s\n' "${@}" | jq -Rsc 'split("\n") | map(select(length > 0) | tonumber)')

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

echo "Requesting HDC statuses for booking IDs: $*"
curl --fail-with-body --silent --show-error --globoff \
  "$BASE_URL$API_PATH" \
  --request POST \
  --header "Authorization: Bearer $TOKEN" \
  --header "Accept: application/json" \
  --header "Content-Type: application/json" \
  --data "$BOOKING_IDS_JSON" | jq .
