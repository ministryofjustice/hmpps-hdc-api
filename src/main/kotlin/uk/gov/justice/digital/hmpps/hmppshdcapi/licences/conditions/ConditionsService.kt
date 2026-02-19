package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedLicenseBatch
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedLicenseConditions

@Service
class ConditionsService {

  @Autowired
  private lateinit var licenceRepository: LicenceRepository

  fun getBespokeConditions(licenceIds: List<Long>): ConvertedLicenseBatch {
    val licences = getLicencesWithAdditionalConditions(licenceIds)
    val batch = licences.map {
      val conditions = LicenceConditionRenderer.renderConditions(it, it.additionalConditionsVersion)
      log.debug("Found {} conditions for licence {}", conditions.size, it.bookingId)
      ConvertedLicenseConditions(it.id!!, it.prisonNumber ?: "", it.bookingId, conditions)
    }

    return ConvertedLicenseBatch(licenceIds, batch)
  }

  private fun getLicencesWithAdditionalConditions(licenceIds: List<Long>): List<Licence> {
    log.debug("Getting licences with additional conditions for ids: {}", licenceIds)
    val licences = licenceRepository.findAllById(licenceIds)
    log.debug("Found {} licences", licences.size)
    return licences.filter { it.licence?.licenceConditions?.additional?.isEmpty() == false }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
