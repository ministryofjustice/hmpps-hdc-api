#
# This script is used to run the HDC API locally with a Postgresql container.
#
# It runs with a combination of properties from the default spring profile (in application.yaml) and supplemented
# with the -dev profile (from application-dev.yml). The latter overrides some of the defaults.
#
# The environment variables here will also override values supplied in spring profile properties, specifically
# around removing the SSL connection to the database and setting the DB properties, SERVER_PORT and client credentials
# to match those used in the docker-compose files.
#
#
stop_docker () {
  # Stop the back end containers
  echo "Bringing down current containers ..."
  docker compose down --remove-orphans
}

restart_docker () {
  stop_docker

  #Prune existing containers
  #Comment in if you wish to perform a fresh install of all containers where all containers are removed and deleted
  #You will be prompted to continue with the deletion in the terminal
  #docker system prune --all

  echo "Pulling back end containers ..."
  docker compose pull
  docker compose -f docker-compose.yml up -d
}

wait_for_docker () {
  echo "Waiting for back end containers to be ready ..."
    until [ "`docker inspect -f {{.State.Running}} licences-api-db`" == true ]; do
        sleep 0.1;
    done;
    until [ "`docker inspect -f {{.State.Health.Status}} localstack-hdc-api`" == "healthy" ]; do
        sleep 0.1;
    done;

    echo "Back end containers are now ready"
}

# Server port - avoid clash with prison-api
export SERVER_PORT=8089

# Client id/secret to access local container-hosted services
# Matches with the seeded client details in hmpps-auth for its dev profile
export SYSTEM_CLIENT_ID=$(kubectl -n licences-dev get secrets hmpps-hdc-api -o json  | jq -r '.data.SYSTEM_CLIENT_ID | @base64d')
export SYSTEM_CLIENT_SECRET=$(kubectl -n licences-dev get secrets hmpps-hdc-api -o json  | jq -r '.data.SYSTEM_CLIENT_SECRET | @base64d')

# Provide the DB connection details to local container-hosted Postgresql DB
# Match with the credentials set in create-and-vary-a-licence/docker-compose.yml
export DB_SERVER=localhost
export DB_USER=licences


# Provide URLs to other local container-based dependent services
# Match with ports defined in docker-compose.yml
export HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth

# Make the connection without specifying the sslmode=verify-full requirement
export SPRING_DATASOURCE_URL='jdbc:postgresql://${DB_SERVER}/${DB_NAME}'

if [[ $1 == "--connect-db" ]]; then
  # connect to DB running from frontend app
  export DB_NAME=licences
  export DB_PASS=licences
  echo "Starting the API locally with separate licences DB"
  SPRING_PROFILES_ACTIVE=stdout,dev ./gradlew bootRun

else
  # connect to DB running from local docker-compose app
  export DB_NAME=hmpps-hdc-api
  export DB_PASS=hmpps-hdc-api

  export SPRING_DATASOURCE_URL='jdbc:postgresql://${DB_SERVER}/${DB_NAME}'
  if [[ $1 == "--start-docker" ]]; then
    restart_docker
  fi
  wait_for_docker
  echo "Starting the API locally"
  SPRING_PROFILES_ACTIVE=stdout,dev,flyway ./gradlew bootRun
fi