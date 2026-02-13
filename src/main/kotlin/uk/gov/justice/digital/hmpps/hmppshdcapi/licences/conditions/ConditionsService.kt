package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedBespokeCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedLicenseBatch
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedLicenseConditions

@Service
class ConditionsService {

  @Autowired
  private lateinit var licenceRepository: LicenceRepository

  fun getBespokeConditions(ids: List<Long>): ConvertedLicenseBatch {
    val licences = getLicencesWithAdditionalConditions(ids)
    val batch = licences.map {
      val conditions = LicenceConditionRenderer.renderConditions(it)
        .map { ConvertedBespokeCondition(it.first, it.second) }
      ConvertedLicenseConditions(it.id!!, it.prisonNumber, it.bookingId, conditions)
    }

    return ConvertedLicenseBatch(ids, batch)
  }

  private fun getLicencesWithAdditionalConditions(ids: List<Long>): List<Licence> {
    val licences = licenceRepository.findAllById(ids)
    return licences.filter { it.licence?.licenceConditions?.additional?.isEmpty() == false }
  }
}
