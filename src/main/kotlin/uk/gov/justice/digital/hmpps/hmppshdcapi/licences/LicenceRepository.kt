package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LicenceRepository : JpaRepository<Licence, Long>, JpaSpecificationExecutor<Licence> {
  fun findAllByPrisonNumber(prisonNumber: String): List<Licence>

  @Query("select l from Licence l where l.deletedAt is null and l.id > ?1 order by l.id asc")
  fun findAllByDeletedAtAndIdGreaterThanLastProcessedOrderByIdAsc(lastProcessed: Long, pageable: Pageable): Page<Licence>

  @Query("select l from Licence l where l.deletedAt is null order by l.id asc")
  fun findAllByDeletedAtOrderByIdAsc(pageable: Pageable): Page<Licence>
}
