# hmpps-hdc-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-hdc-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-hdc-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-hdc-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-hdc-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-hdc-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-hdc-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hdc-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

This is an API for accessing HDC data.

Currently, the main DB definition exists in the [licences](https://github.com/ministryofjustice/licences) repo and the schema is managed by knex migrations.
Any schema changes need to be applied to that repo. 

A copy of the schema exists in this repo for aid of local development - in the short term, any schema changes that are 
applied via knex migrations to the production DBs will need to be back ported to this repo. 

The aim is to make this project manage migrations in the mid/long term.

## Running locally

There is a `run-local.sh` script that can be run to start and stop docker containers and build and run the application.

There are currently 3 modes:

1. `./run-local.sh --start-docker` - starts docker images, waits for them to start and then runs the app against a standalone licences DB 
2. `./run-local.sh` - waits/checks that the requisite docker images exist and then runs the app against a standalone licences DB
3. `./run-local.sh --connect-db` - runs the app against the frontend licences DB docker image (requires localstack running)  