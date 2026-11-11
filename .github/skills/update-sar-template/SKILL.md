---
name: update-sar-template
description: How to update the Subject Access Request (SAR) Handlebars template, the SAR data model/builder, and how to regenerate the expected snapshot files used by the SAR tests in this repo. Use when asked to add/change a field in the SAR report, edit sar_hmpps-hdc-api.mustache, or regenerate SAR snapshots/expected files.
metadata:
  author: hmpps-hdc-api
  version: "1.0"
---

# Updating Subject Access Request (SAR) templates

This service exposes a Subject Access Request (SAR) data API and an HTML report, built using the
`hmpps-subject-access-request-lib`. Changes to what data is returned or rendered require touching three
layers, in this order, and then regenerating snapshot files.

## 1. Data model — what fields exist

Under `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppshdcapi/`:

- `licences/subjectaccessrequests/Content.kt` — top-level content returned by the SAR API (`licences`, `licenceVersions`).
- `model/sar/SARLicenceData.kt` — the per-licence SAR DTOs.

Add/modify fields here first.

## 2. Mapping — how fields are populated

- `model/sar/SARLicenceDataBuilder.kt` — `toSAR()` extension functions mapping stored licence data onto the SAR DTOs.
- `model/sar/SARConditionFormatter.kt` — formats licence conditions.
- `licences/subjectaccessrequests/SubjectAccessRequestService.kt` — assembles `Content` for a prisoner/date range.

Prefer null-guarding data in the builder over conditional logic in the template; the template should just render what is present.

## 3. Handlebars template — how fields are rendered

`src/main/resources/templates/sar_hmpps-hdc-api.mustache` renders the data into the HTML report shown to the SAR team.
It uses Handlebars with the helpers from `hmpps-subject-access-request-lib` (e.g. `optionalValue`, `formatDate`) and
inline partials (`{{#*inline "licenceDetails"}}`). Follow the existing conventions in the file:

- Wrap scalar values in `{{ optionalValue path.to.field }}`.
- Use `{{#obj}}...{{/obj}}` for the populated case and `{{^obj}}<p>No Data Held</p>{{/obj}}` for the absent case.
- Lists: `{{#list}}...{{/list}}` with a `{{^list}}` "No data held" fallback.
- Licences and licence versions share the `licenceDetails` partial, so a change there appears in both.

## 4. Tests and regenerating snapshots

Tests are in `src/test/kotlin/uk/gov/justice/digital/hmpps/hmppshdcapi/integration/`:

| Test | Verifies | Expected file (`src/test/resources/sar/`) |
|---|---|---|
| `SubjectAccessRequestIntegrationTest` via `SarApiDataTest` | JSON from the SAR data API | `sar-api-response.json` |
| via `SarReportTest` | Rendered HTML report | `sar-generated-report.html` |
| via `SarJpaEntitiesTest` | JPA entity schema shape | `entity-schema.json` |
| via `SarFlywaySchemaTest` | Flyway version pinned in `application-test.yml` (`hmpps.sar.tests.expected-flyway-schema-version`) | n/a |
| `SAR template should render all fields in the data model` | Every `Content` model field is accessed by the template (`SarTemplateFieldTracker`) | n/a |

Test data is loaded by `SarTestDataLoader` from `src/test/resources/test_data/sar/*.json`. To cover a new field,
add it to one of these JSON files. If a field intentionally isn't rendered, add it to `IGNORED_SAR_PATHS` in
`SubjectAccessRequestIntegrationTest` with a comment explaining why.

**After changing the model/builder/template, regenerate the expected files instead of hand-editing them:**

```shell
./gradlew updateSarSnapshots
```

**Always run `updateSarSnapshots`, never `generateSarSnapshots` directly.** `generateSarSnapshots` is an internal
step that only writes raw `.log` files (unpretty-printed, in the wrong place) and does not update the snapshots.

`updateSarSnapshots` runs `SubjectAccessRequestIntegrationTest` with `SAR_GENERATE_ACTUAL=true` (via `generateSarSnapshots`),
copies the resulting `.log` files from `src/test/resources/` into `src/test/resources/sar/` (stripping `.log`) and
removes the intermediates. It then pretty-prints the two `.json` snapshots (2-space indent, no trailing newline) because
the generator writes them minified; this keeps the committed snapshots and their diffs readable.

Manual alternative (you must then pretty-print `sar-api-response.json` and `entity-schema.json` yourself with a 2-space
indent and no trailing newline, e.g. `jq --indent 2 . f | perl -pe 'chomp if eof' > tmp && mv tmp f`):

```shell
SAR_GENERATE_ACTUAL=true ./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.integration.SubjectAccessRequestIntegrationTest"
mv src/test/resources/sar-api-response.json.log src/test/resources/sar/sar-api-response.json
mv src/test/resources/sar-generated-report.html.log src/test/resources/sar/sar-generated-report.html
mv src/test/resources/entity-schema.json.log src/test/resources/sar/entity-schema.json
```

Then re-run **without** `SAR_GENERATE_ACTUAL` to confirm it passes:

```shell
./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.hmppshdcapi.integration.SubjectAccessRequestIntegrationTest"
```

Preview the PDF version of the report:

```shell
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome \
  --headless --disable-gpu --print-to-pdf=example-hmpps-hdc-api.pdf src/test/resources/sar/sar-generated-report.html
```

**Always review the diff in `src/test/resources/sar/` before committing** — an unreviewed diff can silently leak or
drop personal data from the SAR report.

## 5. Entity schema / Flyway version

If a change adds/modifies a JPA entity, `SarJpaEntitiesTest` is regenerated by the same run. If a Flyway migration
changes the schema version, update `hmpps.sar.tests.expected-flyway-schema-version` in
`src/test/resources/application-test.yml` and check with the team whether the SAR data/report needs updating for the
new column/table.
