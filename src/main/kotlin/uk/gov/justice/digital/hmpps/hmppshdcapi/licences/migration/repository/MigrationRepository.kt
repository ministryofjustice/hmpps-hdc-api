package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence

interface LicenceBookingDetail {
  val licenceId: Long
  val bookingId: Long
}

enum class MigrationErrorSource {
  CVL,
  HDC,
}

@Repository
interface MigrationRepository : CrudRepository<Licence, Long> {

  @Modifying
  @Transactional
  @Query(
    value = """
            INSERT INTO licence_migration_log(licence_id, success, retry, message, error_source)  VALUES (:licenceId,:success,:retry,:message,CAST(:source AS migration_error_source))
        """,
    nativeQuery = true,

  )
  fun insertMigrationLog(licenceId: Long, success: Boolean, retry: Boolean, message: String? = null, source: String? = null): Int

  @Query(
    value = """
        SELECT message FROM licence_migration_log WHERE licence_id = :licenceId and success = :success and retry = :retry
  """,
    nativeQuery = true,
  )
  fun getMigrationLog(licenceId: Long, success: Boolean, retry: Boolean): String?

  @Query(
    value = """
        SELECT count(*) FROM licence_migration_log
  """,
    nativeQuery = true,
  )
  fun getMigrationLogCount(): Int

  @Query(
    value = """
    SELECT EXISTS (
      SELECT 1 FROM licence_migration_log WHERE licence_id = :licenceId and success = :success
    )
  """,
    nativeQuery = true,
  )
  fun migrationLogExists(licenceId: Long, success: Boolean): Boolean

  /**
   * AND (l.licence -> 'document' -> 'template' IS NOT null)  mean it has been printed!
   */
  @Query(
    value = """
        SELECT l.id AS licenceId, l.booking_id AS bookingId FROM licences l
        LEFT JOIN (
          SELECT DISTINCT ON (licence_id) licence_id, success, retry FROM licence_migration_log ORDER BY licence_id, id DESC
        ) m ON l.id = m.licence_id
        WHERE
            l.id > :lastProcessedId 
            AND l.stage = 'DECIDED' AND l.deleted_at IS NULL
            AND (l.licence -> 'curfew' -> 'approvedPremisesAddress' IS NOT NULL
              OR l.licence -> 'bassReferral' -> 'approvedPremisesAddress' IS NOT NULL
              OR l.licence -> 'proposedAddress' -> 'curfewAddress' IS NOT NULL
              OR l.licence -> 'bassReferral' -> 'bassOffer' IS NOT NULL)
            AND (l.licence -> 'document' -> 'template' IS NOT NULL) 
            AND (m.licence_id IS NULL OR (m.success = false AND m.retry = true))               
        ORDER BY l.id
        LIMIT :batchSize
  """,
    nativeQuery = true,
  )
  fun getMigratableLicences(
    lastProcessedId: Long,
    batchSize: Int,
  ): List<LicenceBookingDetail>

  @Query(
    value = """
        SELECT l.* FROM licences l
        LEFT JOIN (
          SELECT DISTINCT ON (licence_id) licence_id, success, retry FROM licence_migration_log ORDER BY licence_id, id DESC
        ) m ON l.id = m.licence_id
        WHERE
            l.id = :licenceId 
            AND l.stage = 'DECIDED' AND l.deleted_at IS NULL
            AND (l.licence -> 'curfew' -> 'approvedPremisesAddress' IS NOT NULL
              OR l.licence -> 'bassReferral' -> 'approvedPremisesAddress' IS NOT NULL
              OR l.licence -> 'proposedAddress' -> 'curfewAddress' IS NOT NULL
              OR l.licence -> 'bassReferral' -> 'bassOffer' IS NOT NULL)
            AND (l.licence -> 'document' -> 'template' IS NOT NULL) 
            AND (m.licence_id IS NULL OR (m.success = false AND m.retry = true))   
        ORDER BY l.id
        LIMIT 1
  """,
    nativeQuery = true,
  )
  fun getMigratableLicence(
    licenceId: Long,
  ): Licence?
}
