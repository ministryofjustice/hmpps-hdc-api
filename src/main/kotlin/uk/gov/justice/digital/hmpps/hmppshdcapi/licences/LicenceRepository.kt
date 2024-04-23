package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LicenceRepository : JpaRepository<Licence, Long>, JpaSpecificationExecutor<Licence> {
  fun findByPrisonNumber(prisonNumber: String, pageable: Pageable): Page<Licence>
  fun findAllByPrisonNumber(prisonNumber: String): List<Licence>
  fun findAllByDeletedAtAndIdGreaterThanLastProcessedOrderByIdAsc(deletedAt: LocalDateTime?, lastProcessed: Long, pageable: Pageable): Page<Licence>
}
