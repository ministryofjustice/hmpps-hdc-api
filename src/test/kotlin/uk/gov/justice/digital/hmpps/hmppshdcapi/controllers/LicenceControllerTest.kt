package uk.gov.justice.digital.hmpps.hmppshdcapi.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceController
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceService
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@WebMvcTest(controllers = [LicenceController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [LicenceController::class])
@WebAppConfiguration
class LicenceControllerTest {

  @MockBean
  private lateinit var licenceService: LicenceService

  @Autowired
  private lateinit var mvc: MockMvc

  @Autowired
  private lateinit var mapper: ObjectMapper

  @BeforeEach
  fun reset() {
    reset(licenceService)

    mvc = MockMvcBuilders
      .standaloneSetup(
        LicenceController(
          licenceService,
        ),
      )
      .build()
  }

  @Test
  fun `get a licence by booking id`() {
    whenever(licenceService.getByBookingId(1)).thenReturn(aLicence)

    val result = mvc.perform(MockMvcRequestBuilders.get("/licence/hdc/1").accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn()

    assertThat(result.response.contentAsString)
      .isEqualTo(mapper.writeValueAsString(aLicence))

    verify(licenceService, times(1)).getByBookingId(1)
  }

  private companion object {
    val aLicence = HdcLicence(
      CurfewAddress(
        addressLine1 = "123 Approved Premises Street 2",
        addressLine2 = "Off St Michaels Place",
        addressTown = "Leeds",
        postCode = "LS1 2AA",
      ),
      FirstNight(
        firstNightFrom = "15:00",
        firstNightUntil = "07:00",
      ),
      listOf(
        CurfewTimes(
          1L,
          "Monday",
          LocalTime.of(19, 0),
          "Tuesday",
          LocalTime.of(7, 0),
          1L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Tuesday",
          LocalTime.of(19, 0),
          "Wednesday",
          LocalTime.of(7, 0),
          2L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Wednesday",
          LocalTime.of(19, 0),
          "Thursday",
          LocalTime.of(7, 0),
          3L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Thursday",
          LocalTime.of(19, 0),
          "Friday",
          LocalTime.of(7, 0),
          4L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Friday",
          LocalTime.of(19, 0),
          "Saturday",
          LocalTime.of(7, 0),
          5L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Saturday",
          LocalTime.of(19, 0),
          "Sunday",
          LocalTime.of(7, 0),
          6L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
        CurfewTimes(
          1L,
          "Sunday",
          LocalTime.of(19, 0),
          "Monday",
          LocalTime.of(7, 0),
          7L,
          LocalDateTime.of(2024, 8, 14, 9, 0),
        ),
      ),
    )
  }
}
