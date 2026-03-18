up
# hmpps-hdc-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-hdc-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-hdc-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-hdc-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-hdc-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-hdc-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-hdc-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hdc-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

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
3. `./run-local.sh --connect-db` - runs the app against the frontend local licences DB docker image (requires localstack running) 

### Linting

To locate any linting issues

`$ ./gradlew ktlintcheck`

To apply some fixes following linting

`$ ./gradlew ktlintformat` 

### Static Code Analysis

We use a tool called Detekt locally to provide code smell
analysis for the project. A baseline currently exists with all the existing
issues at `detekt-baseline.yml`.

To generate/re-generate the baseline:

`$ ./gradlew detektBaseline`

To run Detekt:

`$ ./gradlew detekt`

Detekt provides some helpful reports in HTML as well which can be opened in any
browser. These are available at `build/reports/detekt`.

### Re-generate the schema

In DBeaver or whichever database tool you are using, establish a database connection then go to the navigator panel on the left. 
Go into `Tables`, select the `Tables` folder and all tables within it. Then right click and Choose `Generate SQL` >> `DDL`. Make 
sure you select the following options:

* Use fully qualified names
* Show comments
* Show permissions
* Show full DDL

Then `Copy` and paste the generated SQL into `src/main/resources/migration/common/V01__init.sql`. Compare the commit changes in 
the file to make sure all looks as expected and there are just the additions required made. Also make sure there are no passwords 
in there before you commit. Replaces all of these passwords with `licences`.

## Subject access requests

This project makes use of the subject access request test library.

If we persist any additional information about an offender, as a team we need consider whether we need to include this
in the SAR responses.

Any changes to data models or API responses that will affect the SAR responses should be reflected in the SAR snapshots.
These are stored in `src/test/resources/sar` and are used to compare against the actual responses received
from the API when running the SAR integration tests.

To regenerate SAR snapshots - use the following command, which will run the SAR integration tests and update the
snapshots with the actual responses received from the API. This is useful to do if you have made changes to the API that
will affect the SAR responses, and you want to make sure the snapshots are up to date with the new expected responses.
```bash
SAR_GENERATE_ACTUAL=true \
  ./gradlew clean integrationTest --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.integration.SubjectAccessRequestIntegrationTest"
```

The generated response will appear as log files in `src/test/resources`

To view the generated PDF: 

```bash
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome \
  --headless --disable-gpu --print-to-pdf=example-hmpps-hdc-api.pdf src/test/resources/sar/sar-generated-report.html
```