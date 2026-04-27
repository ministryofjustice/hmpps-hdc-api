package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.Content
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import javax.sql.DataSource

@Import(SarIntegrationTestHelperConfig::class)
class SubjectAccessRequestIntegrationTest :
  SqsIntegrationTestBase(),
  SarApiDataTest,
  SarFlywaySchemaTest,
  SarReportTest,
  SarJpaEntitiesTest {
  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var sarTestDataLoader: SarTestDataLoader

  override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper

  override fun getWebTestClientInstance(): WebTestClient = webTestClient

  override fun getDataSourceInstance(): DataSource = dataSource

  override fun getEntityManagerInstance(): EntityManager = entityManager

  override fun getPrn(): String = SAR_PRN

  override fun setupTestData() {
    sarTestDataLoader.load()
  }

  @Test
  @Sql("classpath:test_data/reset.sql")
  override fun `SAR API should return expected data`() {
    super.`SAR API should return expected data`()
  }

  @Test
  @Sql("classpath:test_data/reset.sql")
  override fun `SAR report should render as expected`() {
    super.`SAR report should render as expected`()
  }

  @Disabled
  @Test
  @Sql("classpath:test_data/reset.sql")
  fun `SAR template should render all fields in the data model`() {
    sarTestDataLoader.load()

    val dataResponse = sarIntegrationTestHelper.requestSarData(getPrn(), getCrn(), getFromDate(), getToDate(), webTestClient)

    val result = SarTemplateFieldTracker.check(
      content = dataResponse.content,
      modelClass = Content::class,
      ignoredPaths = IGNORED_SAR_PATHS,
    )

    result.expectedPaths.forEach {
      val segments = it // .split("(\\.)|(\\[])".toRegex())
      println(segments)
    }

    val extraInfo = if (result.pathsWithNoTestData.isNotEmpty()) {
      "\n\nThe following model fields have no test data and could not be verified:\n" +
        result.pathsWithNoTestData.joinToString("\n") { "  ? $it" } +
        "\nConsider adding test data to a file under test_data/sar/, or add them to IGNORED_SAR_PATHS."
    } else {
      ""
    }

    assertThat(result.unaccessedPaths)
      .withFailMessage {
        "The following fields are declared in the SAR data model but are not rendered by the mustache template.\n" +
          "Either add them to the template, or add the path to IGNORED_SAR_PATHS with a comment explaining why:\n" +
          result.unaccessedPaths.joinToString("\n") { "  - $it" } +
          extraInfo
      }
      .isEmpty()

    assertThat(result.pathsWithNoTestData)
      .withFailMessage {
        "The following model fields have no test data and cannot be verified for template coverage.\n" +
          "Either populate them in a file under test_data/sar/, or add the path to IGNORED_SAR_PATHS:\n" +
          result.pathsWithNoTestData.joinToString("\n") { "  ? $it" }
      }
      .isEmpty()
  }

  companion object {
    private const val SAR_PRN = "A1234AA"

    /**
     * Paths that are intentionally omitted from the SAR mustache template.
     * Prefix matching is supported: ignoring "foo.bar" also covers "foo.bar.baz".
     *
     * Add new entries here (with a comment) rather than adding fields to the template
     * when the data is genuinely not relevant to the subject of the request.
     */
    private val IGNORED_SAR_PATHS = setOf(
      // Internal system flag recording whether the licence has been migrated to CVL.
      "licences[].licenceInCvl",
      "licenceVersions[].licenceInCvl",

      // Not possible to refuse approve premises for BASS this via the app so this is always null. If it becomes populated in future then we should add it to the template.
      "licences[].licence.bassRejections[].approvedPremisesAddress.addressLine1",
      "licences[].licence.bassRejections[].approvedPremisesAddress.addressLine2",
      "licences[].licence.bassRejections[].approvedPremisesAddress.addressTown",
      "licences[].licence.bassRejections[].approvedPremisesAddress.postCode",
      "licences[].licence.bassRejections[].approvedPremisesAddress.telephone",

      "licenceVersions[].licence.bassRejections[].approvedPremisesAddress.addressLine1",
      "licenceVersions[].licence.bassRejections[].approvedPremisesAddress.addressLine2",
      "licenceVersions[].licence.bassRejections[].approvedPremisesAddress.addressTown",
      "licenceVersions[].licence.bassRejections[].approvedPremisesAddress.postCode",
      "licenceVersions[].licence.bassRejections[].approvedPremisesAddress.telephone",
    )
  }
}
