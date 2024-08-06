package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LicenceVersionRepository : JpaRepository<LicenceVersion, Long>, JpaSpecificationExecutor<LicenceVersion> {
  fun findAllByPrisonNumber(prisonNumber: String): List<LicenceVersion>
  fun findAllByBookingIdAndDeletedAtIsNull(bookingId: Long): List<LicenceVersion>

  @Modifying
  @Query("update LicenceVersion lv set lv.deletedAt = ?1 where lv.bookingId in ?2 and lv.deletedAt is null")
  fun softDeleteLicenceVersions(now: LocalDateTime, bookingIds: List<Long>)
}
