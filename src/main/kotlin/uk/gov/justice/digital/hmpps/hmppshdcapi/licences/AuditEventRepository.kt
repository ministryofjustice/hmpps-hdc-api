package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AuditEventRepository :
  JpaRepository<AuditEvent, Long>,
  JpaSpecificationExecutor<AuditEvent> {

  @Query(
    nativeQuery = true,
    value = """
        select * from audit order by timestamp
    """,
  )
  fun findByBookingId(bookingId: String): List<AuditEvent>
}
