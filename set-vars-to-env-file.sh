#!/bin/bash
#
# This script creates a .env file to allow setting env vars to run the Spring app locally in an IDE.
# Run with: ./set-vars-to-local-env.sh
#
# Add the following to IntelliJ Run/Debug config's "Environment variables":
#    /Users/<<YOUR-USER-DIR>>/env-config/hdc-api.env
#
# (Requires Docker + kubectl + jq)
#

set -e

# --- Strings (static values) ---
export SERVER_PORT=8089
export DB_SERVER=localhost
export DB_NAME=licences
export DB_USER=licences
export DB_PASS=licences
export HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_SERVER}/${DB_NAME}"

echo "Getting SYSTEM_CLIENT_ID..."
export SYSTEM_CLIENT_ID=$(kubectl -n licences-dev get secrets hmpps-hdc-api -o json  | jq -r '.data.SYSTEM_CLIENT_ID | @base64d')
echo "Getting SYSTEM_CLIENT_SECRET..."
export SYSTEM_CLIENT_SECRET=$(kubectl -n licences-dev get secrets hmpps-hdc-api -o json  | jq -r '.data.SYSTEM_CLIENT_SECRET | @base64d')

# --- Booleans / Flags ---


# --- Write to .env file ---
fileDir=~/env-config/
mkdir -p "$fileDir"
cd "$fileDir"
fileToAddVars='hdc-api.env'

echo "Writing environment variables to $fileDir$fileToAddVars..."

cat > "$fileToAddVars" <<EOF
# --- Strings ---
  SERVER_PORT=${SERVER_PORT}
  DB_SERVER=${DB_SERVER}
  DB_NAME=${DB_NAME}
  DB_USER=${DB_USER}
  DB_PASS=${DB_PASS}
  HMPPS_AUTH_URL=${HMPPS_AUTH_URL}
  SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}

# --- Secrets ---
  SYSTEM_CLIENT_ID="${SYSTEM_CLIENT_ID}"
  SYSTEM_CLIENT_SECRET="${SYSTEM_CLIENT_SECRET}"

# --- Flags ---

EOF

echo "✅ Done. Environment variables saved to: $fileDir$fileToAddVars"
