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
 * Scalar metadata (booking IDs, timestamps, etc.) is defined here as typed constants.
 *
 * Explicit IDs are used to match the original SQL data exactly.
 * Native queries are needed because the Licence and LicenceVersion entities use
 * @GeneratedValue(IDENTITY), which prevents setting IDs via repository.save().
 */
@Component
class SarTestDataLoader(
  private val entityManager: EntityManager,
  private val objectMapper: ObjectMapper,
) {

  @Transactional
  fun load() {
    insertLicences()
    insertLicenceVersions()
  }

  private fun insertLicences() {
    val defaultTransitionDate = LocalDateTime.of(2021, 8, 6, 15, 6, 37, 188_000_000)

    data class LicenceRow(
      val id: Long,
      val jsonFile: String,
      val bookingId: Long,
      val deletedAt: LocalDateTime? = null,
    )

    val rows = listOf(
      LicenceRow(1530, "standard-bass-offer", bookingId = 10),
      LicenceRow(1531, "bass-complex-with-rejections", bookingId = 11, deletedAt = LocalDateTime.of(2023, 3, 8, 11, 27, 45, 257_000_000)),
      LicenceRow(1532, "residential-with-approved-premises", bookingId = 10),
      LicenceRow(1533, "residential-new-proposed-address", bookingId = 10),
      LicenceRow(1534, "excluded", bookingId = 10),
      LicenceRow(1535, "eligibility-scraped-through", bookingId = 10),
      LicenceRow(1536, "v1-risk-address-refused", bookingId = 10),
      LicenceRow(1537, "v1-risk-postponed", bookingId = 10),
      LicenceRow(1538, "v1-risk-refused-by-ca", bookingId = 10),
      LicenceRow(1539, "v1-risk-consent-homeowner", bookingId = 10),
      LicenceRow(1540, "v2-appointment-details", bookingId = 10),
      LicenceRow(1541, "variation", bookingId = 10),
      LicenceRow(1542, "out-of-time-refusal", bookingId = 10),
      LicenceRow(1543, "approval-with-release-reason", bookingId = 10),
    )

    rows.forEach { row ->
      val licenceJson = loadJson("test_data/sar/licences/${row.jsonFile}.json")
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

  private fun insertLicenceVersions() {
    val decTimestamp = LocalDateTime.of(2023, 12, 3, 11, 1, 35, 913_000_000)
    val marTimestamp = LocalDateTime.of(2023, 3, 8, 11, 27, 45, 257_000_000)
    val octTimestamp = LocalDateTime.of(2023, 10, 3, 11, 1, 35, 913_000_000)
    val decDeletedAt = LocalDateTime.of(2024, 4, 12, 11, 27, 45, 257_000_000)

    data class LicenceVersionRow(
      val id: Long,
      val jsonFile: String,
      val bookingId: Long,
      val version: Int,
      val varyVersion: Int,
      val timestamp: LocalDateTime,
      val deletedAt: LocalDateTime? = null,
    )

    val rows = listOf(
      // booking_id=10, version=29
      LicenceVersionRow(422, "residential-with-rejection-bk10-v29", bookingId = 10, version = 29, varyVersion = 0, timestamp = octTimestamp),
      LicenceVersionRow(423, "bass-offer-accepted-bk10-v29-deleted", bookingId = 10, version = 29, varyVersion = 0, timestamp = decTimestamp, deletedAt = decDeletedAt),
      LicenceVersionRow(424, "residential-approved-premises-bk10-v29-deleted", bookingId = 10, version = 29, varyVersion = 0, timestamp = decTimestamp, deletedAt = decDeletedAt),
      LicenceVersionRow(425, "residential-new-address-bk10-v29-deleted", bookingId = 10, version = 29, varyVersion = 0, timestamp = decTimestamp, deletedAt = decDeletedAt),
      LicenceVersionRow(426, "excluded-bk10-v29-deleted", bookingId = 10, version = 29, varyVersion = 0, timestamp = decTimestamp, deletedAt = decDeletedAt),
      LicenceVersionRow(427, "eligibility-scraped-bk10-v29-deleted", bookingId = 10, version = 29, varyVersion = 0, timestamp = decTimestamp, deletedAt = decDeletedAt),
      LicenceVersionRow(428, "v1-risk-address-refused-bk10-v29", bookingId = 10, version = 29, varyVersion = 3, timestamp = decTimestamp),
      LicenceVersionRow(429, "v1-risk-postponed-bk10-v29", bookingId = 10, version = 29, varyVersion = 1, timestamp = decTimestamp),
      LicenceVersionRow(430, "v1-risk-refused-ca-bk10-v29", bookingId = 10, version = 29, varyVersion = 2, timestamp = decTimestamp),
      // booking_id=20, version=3
      LicenceVersionRow(431, "residential-with-rejection-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 0, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(432, "bass-complex-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 0, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(433, "v2-appointment-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 0, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(434, "v1-consent-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 2, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(435, "rejected-addresses-bass-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 4, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(436, "variation-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 4, timestamp = marTimestamp, deletedAt = marTimestamp),
      LicenceVersionRow(437, "out-of-time-refusal-bk20-v3-deleted", bookingId = 20, version = 3, varyVersion = 4, timestamp = marTimestamp, deletedAt = marTimestamp),
    )

    rows.forEach { row ->
      val licenceJson = loadJson("test_data/sar/licence-versions/${row.jsonFile}.json")
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
