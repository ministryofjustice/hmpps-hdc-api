# hmpps-hdc-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-hdc-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-hdc-api "Link to report")
[![Pipeline](https://github.com/ministryofjustice/hmpps-hdc-api/actions/workflows/pipeline.yml/badge.svg)](https://github.com/ministryofjustice/hmpps-hdc-api/actions/workflows/pipeline.yml)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hdc-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

This is an API for accessing HDC (Home Detention Curfew) data. It is a Kotlin / Spring Boot service (Java 25) backed by PostgreSQL,
and also consumes domain events from SQS.

## Database

Currently, the main DB definition exists in the [licences](https://github.com/ministryofjustice/licences) repo and the schema is managed by knex migrations.
Any schema changes need to be applied to that repo. 

A copy of the schema exists in this repo for aid of local development - in the short term, any schema changes that are 
applied via knex migrations to the production DBs will need to be back ported to this repo. 

The aim is to make this project manage migrations in the mid/long term.

## Running locally

Prerequisites: Docker, a Java 25 JDK, and `kubectl` + `jq` configured with access to the `licences-dev` namespace
(client credentials are read from the `hmpps-hdc-api` k8s secret).

A `Makefile` starts and stops the docker containers and builds and runs the application (see its header for details).

* `make run` - starts any missing containers, waits for them to be ready and then runs the app against a standalone licences DB
* `make run-skip-docker` - runs the app only, without touching docker
* `make run-skip-docker MODE=db` - runs the app against the frontend local licences DB docker image (requires localstack running)
* `make run-skip-docker MODE=t3` - runs the app against the dev DB (port-forward needs to be running)
* `make stop` / `make clean` / `make prune` - stop containers / also remove volumes and gradle build output / also remove images
* `make update` - updates the gradle wrapper and dependency versions
* Add `DEBUG=1` to run with `--debug-jvm` (this also enables LocalStack debug logging)

The app runs on port 8089.

## Testing

* `./gradlew test` - unit tests
* `./gradlew integrationTest` - integration tests (everything in the `integration` package); requires Docker as Testcontainers starts PostgreSQL and LocalStack
* `./gradlew test --tests "<fully qualified class>"` runs a single test class (use `integrationTest` for integration tests)

## Linting

To locate any linting issues

`$ ./gradlew ktlintCheck`

To apply some fixes following linting

`$ ./gradlew ktlintFormat`

To run ktlint and detekt automatically before each commit, install the git hook with `./gradlew installLocalGitHook`.

## Static code analysis

We use a tool called Detekt locally to provide code smell
analysis for the project. A baseline currently exists with all the existing
issues at `detekt-baseline.xml`.

To generate/re-generate the baseline:

`$ ./gradlew detektBaseline`

To run Detekt:

`$ ./gradlew detekt`

Detekt provides some helpful reports in HTML as well which can be opened in any
browser. These are available at `build/reports/detekt`.

## Re-generating the schema

In DBeaver or whichever database tool you are using, establish a database connection then go to the navigator panel on the left. 
Go into `Tables`, select the `Tables` folder and all tables within it. Then right click and Choose `Generate SQL` >> `DDL`. Make 
sure you select the following options:

* Use fully qualified names
* Show comments
* Show permissions
* Show full DDL

Then `Copy` and paste the generated SQL into `src/main/resources/migration/common/V01__init.sql`. Compare the commit changes in 
the file to make sure all looks as expected and there are just the additions required made. Also make sure there are no passwords 
in there before you commit. Replace all of these passwords with `licences`.

## Subject access requests

This project makes use of the subject access request test library.

If we persist any additional information about an offender, as a team we need consider whether we need to include this
in the SAR responses.

Any changes to data models or API responses that will affect the SAR responses should be reflected in the SAR snapshots.
These are stored in `src/test/resources/sar` and are used to compare against the actual responses received
from the API when running the SAR integration tests.

The simplest way to regenerate the snapshots is `./gradlew updateSarSnapshots`, which runs the SAR integration test with
`SAR_GENERATE_ACTUAL=true` and copies the results into `src/test/resources/sar`. Review the diff before committing.
See the `update-sar-template` skill in `.github/skills` for more detail.

To do it manually - use the following command, which will run the SAR integration tests and update the
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