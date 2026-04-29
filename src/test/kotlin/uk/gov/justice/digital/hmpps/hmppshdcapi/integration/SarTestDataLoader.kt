package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Loads SAR test data into the database via JPA native queries.
 *
 * Each licence JSON payload lives in its own file under test_data/sar/.
 * Not all of these licences are representative of valid licences that can be created in the HDC service.
 *
 * Explicit IDs are used to match the original SQL data exactly.
 */
@Component
class SarTestDataLoader(
  private val entityManager: EntityManager,
  private val objectMapper: ObjectMapper,
) {
  data class Case(
    val id: Long,
    val variationId: Long,
    val jsonFile: String,
    val bookingId: Long = 10,
    val version: Int = 1,
    val varyVersion: Int = 0,
    val timestamp: LocalDateTime,
    val deletedAt: LocalDateTime? = null,
  )

  val decTimestamp = LocalDateTime.of(2023, 12, 3, 11, 1, 35, 913_000_000)
  val marTimestamp = LocalDateTime.of(2023, 3, 8, 11, 27, 45, 257_000_000)
  val octTimestamp = LocalDateTime.of(2023, 10, 3, 11, 1, 35, 913_000_000)

  val cases = listOf(
    Case(
      id = 1530,
      variationId = 230,
      jsonFile = "approval-with-release-reason",
      timestamp = octTimestamp,
      deletedAt = decTimestamp,
    ),
    Case(
      id = 1531,
      variationId = 231,
      jsonFile = "bass-complex-with-rejections",
      timestamp = octTimestamp,
    ),
    Case(
      id = 1532,
      variationId = 232,
      jsonFile = "eligibility-scraped-through",
      timestamp = marTimestamp,
    ),
    Case(
      id = 1533,
      variationId = 233,
      jsonFile = "excluded",
      timestamp = marTimestamp,
    ),
    Case(
      id = 1534,
      variationId = 234,
      jsonFile = "out-of-time-refusal",
      timestamp = decTimestamp,
    ),
    Case(
      id = 1535,
      variationId = 235,
      jsonFile = "standard-bass-offer",
      timestamp = octTimestamp,
    ),
    Case(
      id = 1536,
      variationId = 236,
      jsonFile = "residential-with-approved-premises",
      version = 2,
      varyVersion = 1,
      timestamp = octTimestamp,
    ),
    Case(
      id = 1537,
      variationId = 237,
      jsonFile = "v1-risk-address-refused",
      bookingId = 20,
      timestamp = marTimestamp,
    ),
    Case(
      id = 1538,
      variationId = 238,
      jsonFile = "v1-risk-consent-homeowner",
      bookingId = 20,
      version = 1,
      varyVersion = 1,
      timestamp = octTimestamp,
    ),
    Case(
      id = 1539,
      variationId = 239,
      jsonFile = "v1-risk-postponed",
      bookingId = 20,
      version = 1,
      varyVersion = 1,
      timestamp = octTimestamp,
    ),
    Case(
      id = 1540,
      variationId = 240,
      jsonFile = "v1-risk-refused-by-ca",
      bookingId = 20,
      version = 1,
      varyVersion = 1,
      timestamp = octTimestamp,
    ),
    Case(
      id = 1541,
      variationId = 241,
      jsonFile = "v2-appointment-details",
      bookingId = 20,
      version = 1,
      varyVersion = 1,
      timestamp = octTimestamp,
    ),
    Case(
      id = 1542,
      variationId = 242,
      jsonFile = "variation",
      bookingId = 20,
      version = 3,
      varyVersion = 1,
      timestamp = decTimestamp,
    ),
  )

  @Transactional
  fun load() {
    cases.forEach { row ->
      val licenceJson = loadJson("test_data/sar/${row.jsonFile}.json")
      entityManager.createNativeQuery(
        """
        INSERT INTO licence_versions
          (id, timestamp, licence, booking_id, version, template,
           vary_version, prison_number, deleted_at, licence_in_cvl)
        VALUES
          (:id, :timestamp, CAST(:licence AS jsonb), :bookingId, :version, 'hdc_ap',
           :varyVersion, 'A1234AA', :deletedAt, false)
        """,
      )
        .setParameter("id", row.id)
        .setParameter("timestamp", row.timestamp)
        .setParameter("licence", licenceJson)
        .setParameter("bookingId", row.bookingId)
        .setParameter("version", row.version)
        .setParameter("varyVersion", row.varyVersion)
        .setParameter("deletedAt", octTimestamp)
        .executeUpdate()

      val defaultTransitionDate = LocalDateTime.of(2021, 8, 6, 15, 6, 37, 188_000_000)

      entityManager.createNativeQuery(
        """
        INSERT INTO licences
          (id, licence, booking_id, stage, version, transition_date,
           vary_version, additional_conditions_version, standard_conditions_version,
           prison_number, deleted_at, licence_in_cvl)
        VALUES
          (:id, CAST(:licence AS jsonb), :bookingId, 'PROCESSING_RO', 1, :transitionDate,
           0, 2, 2, 'A1234AA', :deletedAt, false)
        """,
      )
        .setParameter("id", row.id)
        .setParameter("licence", licenceJson)
        .setParameter("bookingId", row.bookingId)
        .setParameter("transitionDate", defaultTransitionDate)
        .setParameter("deletedAt", row.deletedAt)
        .executeUpdate()
    }
  }

  private fun loadJson(resourcePath: String): String {
    val stream = checkNotNull(javaClass.classLoader.getResourceAsStream(resourcePath)) {
      "Test resource not found: $resourcePath"
    }
    return objectMapper.readTree(stream).toString()
  }
}
