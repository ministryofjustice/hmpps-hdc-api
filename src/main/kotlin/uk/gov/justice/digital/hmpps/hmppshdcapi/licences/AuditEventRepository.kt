package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AuditEventRepository : JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {

  @Query(
    nativeQuery = true,
    value = "select id, timestamp, user, action, details from audit where details ->> 'bookingId' in :bookingIds order by timestamp desc;",
  )
  fun findByBookingIds(bookingIds: Collection<String>): List<AuditEvent>
}
