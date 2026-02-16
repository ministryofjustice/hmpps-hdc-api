package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

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
      val conditions = LicenceConditionRenderer.renderConditions(it)
      ConvertedLicenseConditions(it.id!!, it.prisonNumber ?: "", it.bookingId, conditions)
    }

    return ConvertedLicenseBatch(licenceIds, batch)
  }

  private fun getLicencesWithAdditionalConditions(licenceIds: List<Long>): List<Licence> {
    val licences = licenceRepository.findAllById(licenceIds)
    return licences.filter { it.licence?.licenceConditions?.additional?.isEmpty() == false }
  }
}
