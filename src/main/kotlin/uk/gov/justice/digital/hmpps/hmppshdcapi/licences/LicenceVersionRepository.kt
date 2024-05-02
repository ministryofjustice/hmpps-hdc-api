package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface LicenceVersionRepository : JpaRepository<LicenceVersion, Long>, JpaSpecificationExecutor<LicenceVersion> {
  fun findAllByPrisonNumber(prisonNumber: String): List<LicenceVersion>
  fun findAllByBookingId(bookingId: Long): List<LicenceVersion>
}
