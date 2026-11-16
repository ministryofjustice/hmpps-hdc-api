# Run the HDC API locally with PostgreSQL and LocalStack containers (see docker-compose.yml).
#
# Runs with the default spring profile (application.yml) supplemented by the dev profile (application-dev.yml).
# The environment variables below override those profiles: no SSL on the DB connection, DB properties,
# SERVER_PORT and client credentials matching docker-compose.yml.
#
# Targets:
#   run              Start any missing containers, then run the service
#   run-skip-docker  Run the service only (no docker); secrets are only looked up here
#   stop             Stop the containers
#   clean            ./gradlew clean, then remove containers and volumes (keeps images)
#   update           Update the gradle wrapper and dependency versions to the latest
#   prune            Remove containers, volumes and images
#
# Options:
#   DEBUG=1          Run bootRun with --debug-jvm (also passed to docker compose, enabling LocalStack debug)
#   MODE=db          Use the frontend's local licences DB (requires localstack running); use with run-skip-docker
#   MODE=t3          Use the dev RDS DB (port-forward needs to be running); use with run-skip-docker
#                    Default: the docker-compose DB

SHELL := /bin/bash

KUBE_NAMESPACE := licences-dev

# secret,<secret name>,<key>: look up a key from a kubernetes secret
secret = $(shell kubectl -n $(KUBE_NAMESPACE) get secrets $(1) -o json | jq -r '.data.$(2) | @base64d')

GRADLE_ARGS :=
ifdef DEBUG
GRADLE_ARGS += --debug-jvm
endif

.PHONY: run run-skip-docker stop clean update prune

run:
	docker compose up -d --wait --remove-orphans
	$(MAKE) run-skip-docker

# Variables are recursively expanded (=) so secret lookups only happen when this target's recipe runs
run-skip-docker: export SERVER_PORT = 8089
# Client id/secret to access local container-hosted services; matches the seeded client details in hmpps-auth for its dev profile
run-skip-docker: export SYSTEM_CLIENT_ID = $(call secret,hmpps-hdc-api,SYSTEM_CLIENT_ID)
run-skip-docker: export SYSTEM_CLIENT_SECRET = $(call secret,hmpps-hdc-api,SYSTEM_CLIENT_SECRET)
# Bulk migration date toggle
run-skip-docker: export ALLOWED_BULK_CVL_MIGRATION_DATE = 2026-05-11
# DB connection details for the local container-hosted PostgreSQL; match docker-compose.yml
run-skip-docker: export DB_SERVER = localhost
run-skip-docker: export HMPPS_AUTH_URL = https://sign-in-dev.hmpps.service.justice.gov.uk/auth
# Connect without the sslmode=verify-full requirement; ${DB_SERVER} and ${DB_NAME} are resolved by spring
run-skip-docker: export SPRING_DATASOURCE_URL = jdbc:postgresql://$${DB_SERVER}/$${DB_NAME}
ifeq ($(MODE),db)
run-skip-docker: export DB_USER = licences
run-skip-docker: export DB_NAME = licences
run-skip-docker: export DB_PASS = licences
run-skip-docker: export SPRING_PROFILES_ACTIVE = stdout,dev
else ifeq ($(MODE),t3)
run-skip-docker: export DB_NAME = $(call secret,dps-rds-instance-output,database_name)
run-skip-docker: export DB_PASS = $(call secret,dps-rds-instance-output,database_password)
run-skip-docker: export DB_USER = $(call secret,dps-rds-instance-output,database_username)
run-skip-docker: export SPRING_PROFILES_ACTIVE = stdout,dev
else
run-skip-docker: export DB_USER = licences
run-skip-docker: export DB_NAME = hmpps-hdc-api
run-skip-docker: export DB_PASS = hmpps-hdc-api
run-skip-docker: export SPRING_PROFILES_ACTIVE = stdout,dev,flyway
endif
run-skip-docker:
	./gradlew bootRun $(GRADLE_ARGS)

stop:
	docker compose stop

clean:
	./gradlew clean
	docker compose down --remove-orphans --volumes

update:
	./gradlew wrapper --gradle-version latest
	./gradlew useLatestVersions

prune:
	docker compose down --remove-orphans --volumes --rmi all
