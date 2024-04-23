package uk.gov.justice.digital.hmpps.hmppshdcapi.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.PopulateLicenceDeletedAtMigration
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import java.time.LocalDate

class PopulateLicenceDeleteAtMigrationTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val licenceVersionRepository = mock<LicenceVersionRepository>()
  private val prisonApiClient = mock<PrisonApiClient>()

  private val service =
    PopulateLicenceDeletedAtMigration(
      licenceRepository,
      licenceVersionRepository,
      prisonApiClient,
    )

  private val today = LocalDate.of(2024, 4, 16)

  @BeforeEach
  fun reset() {
    reset(licenceRepository, licenceVersionRepository, prisonApiClient)
  }

  @Test
  fun `is to be soft deleted when TUSED is before LED and LED is today`() {
    val booking = booking.copy(topupSupervisionExpiryDate = today.minusDays(1), licenceExpiryDate = today)
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is before LED and LED is in the past`() {
    val booking = booking.copy(topupSupervisionExpiryDate = today.minusDays(2), licenceExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is today`() {
    val booking = booking.copy(topupSupervisionExpiryDate = today)
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is in the past`() {
    val booking = booking.copy(topupSupervisionExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is null but LED is today`() {
    val booking = booking.copy(licenceExpiryDate = today)
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is null but LED is in the past`() {
    val booking = booking.copy(licenceExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isTrue
  }

  @Test
  fun `is not to be soft deleted when both TUSED and LED are null`() {
    val booking = booking.copy(topupSupervisionExpiryDate = null, licenceExpiryDate = null)
    val result = service.isToBeSoftDeleted(booking)

    assertThat(result).isFalse
  }

  private companion object {
    val booking = Booking(
      offenderNo = "A1234BC",
      bookingId = 1,
      agencyId = "MDI",
      topupSupervisionExpiryDate = null,
      licenceExpiryDate = null,
    )
  }
}
