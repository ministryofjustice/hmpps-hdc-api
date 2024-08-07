package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LicenceRepository : JpaRepository<Licence, Long>, JpaSpecificationExecutor<Licence> {
  fun findAllByPrisonNumber(prisonNumber: String): List<Licence>

  @Query("select new uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository\$LicenceIdentifiers(l.id, l.prisonNumber, l.bookingId) from Licence l where l.deletedAt is null and l.id > ?1 order by l.id asc")
  fun findAllByIdGreaterThanLastProcessed(lastProcessed: Long, pageable: Pageable): Page<LicenceIdentifiers>

  @Modifying
  @Query("update Licence l set l.deletedAt = ?1 where l.id in ?2 and l.deletedAt is null")
  fun softDeleteLicence(now: LocalDateTime, ids: List<Long>): Unit

  @Query("select l from Licence l where l.deletedAt is null order by l.id asc")
  fun findAllByDeletedAtOrderByIdAsc(pageable: Pageable): Page<Licence>

  @Query("select l from Licence l where l.deletedAt is null and l.bookingId in ?1 order by l.id asc")
  fun findByBookingIds(bookingIds: List<Long>): List<Licence>

  data class LicenceIdentifiers(
    val id: Long,
    var prisonNumber: String,
    val bookingId: Long,
  )
}
