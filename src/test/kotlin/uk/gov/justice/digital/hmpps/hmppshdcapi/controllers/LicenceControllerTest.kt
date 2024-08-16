package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

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
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

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
      "0800 800 800",
      "123 Approved Premises Street 2, Off St Michaels Place, Leeds, LS1 2AA",
      FirstNight(
        firstNightFrom = "15:00",
        firstNightUntil = "07:00",
      ),
      CurfewHours(
        mondayFrom = "19:00",
        mondayUntil = "07:00",
        tuesdayFrom = "19:00",
        tuesdayUntil = "07:00",
        wednesdayFrom = "19:00",
        wednesdayUntil = "07:00",
        thursdayFrom = "19:00",
        thursdayUntil = "07:00",
        fridayFrom = "19:00",
        fridayUntil = "07:00",
        saturdayFrom = "19:00",
        saturdayUntil = "07:00",
        sundayFrom = "19:00",
        sundayUntil = "07:00",
      ),
    )
  }
}
