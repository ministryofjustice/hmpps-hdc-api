package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface LicenceVersionRepository :
  JpaRepository<LicenceVersion, Long>,
  JpaSpecificationExecutor<LicenceVersion> {

  @Query("Select l.id from LicenceVersion l where l.prisonNumber = ?1")
  fun findAllPrisonIds(prisonNumber: String): List<Long>

  @Transactional
  @Modifying
  @Query("update LicenceVersion l set l.prisonNumber = ?2 where l.prisonNumber = ?1")
  fun updatePrisonNumber(prisonNumber: String, newPrisonNumber: String): Int

  fun findAllByPrisonNumber(prisonNumber: String): List<LicenceVersion>
  fun findAllByBookingIdAndDeletedAtIsNull(bookingId: Long): List<LicenceVersion>

  @Modifying
  @Query("update LicenceVersion lv set lv.deletedAt = ?1 where lv.bookingId in ?2 and lv.deletedAt is null")
  fun softDeleteLicenceVersions(now: LocalDateTime, bookingIds: List<Long>)
}
