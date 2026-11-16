# Copilot instructions

Kotlin / Spring Boot 4 (Java 25 toolchain) HMPPS API exposing HDC (Home Detention Curfew) licence data. Built on the HMPPS Gradle plugin (`uk.gov.justice.hmpps.gradle-spring-boot`) and `hmpps-kotlin-spring-boot-starter`.

## Build, test, lint

```bash
./gradlew build                 # compile + unit tests + ktlint + detekt (check depends on both)
./gradlew test                  # unit tests only (excludes *.integration.* classes)
./gradlew integrationTest       # only classes in the *.integration.* package; needs Docker (Testcontainers: Postgres + LocalStack)
./gradlew test --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.softdelete.SoftDeleteServiceTest"
./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.integration.NotFoundTest"
./gradlew ktlintCheck           # ktlintFormat to auto-fix
./gradlew detekt                # config in detekt.yml, baseline in detekt-baseline.xml (detektBaseline to regenerate)
./gradlew installLocalGitHook   # installs the pre-commit hook from .scripts/pre-commit (ktlint + detekt)
```

Unit vs integration tests are separated purely by package: anything under `...hmppshdcapi.integration` runs in `integrationTest`; everything else runs in `test`.

### Running locally

The `Makefile` (see its header) drives local running. `make run` starts the containers in `docker-compose.yml` (Postgres on 5432, LocalStack gateway on 4666) then runs `bootRun` with `SPRING_PROFILES_ACTIVE=stdout,dev,flyway`. `run-skip-docker` sets `SERVER_PORT=8089` and pulls `SYSTEM_CLIENT_ID/SECRET` from the `licences-dev` k8s secret via `kubectl | jq`, so `kubectl` access is required.

- `make run-skip-docker MODE=db` – use the frontend's local licences DB (`DB_NAME=licences`)
- `make run-skip-docker MODE=t3` – use the dev RDS via port-forward (credentials read from `dps-rds-instance-output` secret)
- `DEBUG=1` adds `--debug-jvm`; `make stop|clean|prune|update` for housekeeping

## Architecture

- Single package root `uk.gov.justice.digital.hmpps.hmppshdcapi`, organised by feature under `licences/` (core licence/HDC status, `conditions`, `events`, `migration`, `prison`, `softdelete`, `subjectaccessrequests`), plus `config/`, `health/`, `model/` (API DTOs, incl. `model/sar`) and `util/`.
- **The DB schema is owned by the separate `licences` repo (knex migrations).** This repo only holds a copy in `src/main/resources/migration/common/V01__init.sql`, applied by Flyway under the `flyway` profile (local/test). Schema changes must be made in `licences` and back-ported here; see README for regenerating the DDL (replace any passwords with `licences`).
- **Events:** `DomainEventListenerService` is an `@SqsListener("domaineventsqueue")` handling prisoner merge and prisoner-released domain events (→ `MergePrisonerService`, `MigrationProcessService`).
- **HDC → CVL migration:** `licences/migration` migrates HDC licences to Create and Vary a Licence via `CvlApiClient`; a bulk-migration cut-off is controlled by the `ALLOWED_BULK_CVL_MIGRATION_DATE` env var.
- **Soft delete / reset:** `licences/softdelete` holds `@Async` jobs triggered through controllers (`SoftDeleteJobController`, `ResetController`, …) that use `PrisonSearchApiClient` and write `AuditEvent`s.
- Security: OAuth2 resource server (`config/ResourceServerConfiguration`, `AuthAwareTokenConverter`); `ResourceSecurityTest` verifies endpoint role protection, so new endpoints need to be covered/annotated consistently.
- JPA entities are made open via `allOpen { annotation("jakarta.persistence.Entity") }` in `build.gradle.kts`.
- Deployment config lives in `helm_deploy/`; CI is GitHub Actions (`.github/workflows`) using shared `hmpps-github-actions` workflows.

## Conventions

- **Subject Access Requests (SAR):** any change to persisted offender data or API/SAR response models must be reflected in the SAR mustache template (`src/main/resources/templates/sar_hmpps-hdc-api.mustache`) and the snapshots in `src/test/resources/sar`. `SarTemplateFieldTracker` guards template fields. Regenerate snapshots with:
  ```bash
  SAR_GENERATE_ACTUAL=true ./gradlew clean integrationTest --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.integration.SubjectAccessRequestIntegrationTest"
  ```
- **Integration test bases** (`integration/base`): `IntegrationTestBase` (random-port Spring Boot + `WebTestClient` + JWT helper via `jwtAuthHelper`, WireMock mock servers under `integration/wiremock` for OAuth and downstream APIs) and `SqsIntegrationTestBase` (adds LocalStack SQS). Containers come from `helpers/PostgresContainer.kt` / `LocalStackContainer.kt`. Extend these rather than creating new `@SpringBootTest` setups.
- Mockito tests use `mockito-kotlin`; assertions use AssertJ.
- Formatting is enforced by ktlint (2-space indent, `.editorconfig`); detekt only analyses `src/main`.
