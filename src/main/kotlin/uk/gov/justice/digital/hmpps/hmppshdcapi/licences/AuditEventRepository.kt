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
      SELECT * FROM audit WHERE details ->> 'bookingId' = :bookingId AND id >= :id ORDER BY timestamp DESC
  """,
  )
  fun findByBookingIdAndAuditId(bookingId: String, id: Long): List<AuditEvent>

  @Query(
    nativeQuery = true,
    value = """
      SELECT a.id FROM audit a WHERE a.details ->> 'bookingId' = :bookingId AND a."action" = 'LICENCE_RECORD_STARTED' ORDER BY timestamp DESC LIMIT 1
  """,
  )
  fun findLicenceRecordStartedAuditId(bookingId: String): Long?
}
