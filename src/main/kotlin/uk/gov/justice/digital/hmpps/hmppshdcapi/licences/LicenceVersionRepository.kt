package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface LicenceVersionRepository : JpaRepository<LicenceVersion, Long>, JpaSpecificationExecutor<LicenceVersion> {
  fun findByPrisonNumber(prisonNumber: String, pageable: Pageable): Page<LicenceVersion>
  fun findAllByPrisonNumber(prisonNumber: String): List<LicenceVersion>
  fun findAllByBookingId(bookingId: Long): List<LicenceVersion>
}
