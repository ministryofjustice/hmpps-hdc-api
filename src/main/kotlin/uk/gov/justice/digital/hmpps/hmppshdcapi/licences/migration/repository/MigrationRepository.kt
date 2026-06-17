package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.response.LicenceMigrationLogEntryDto
import java.time.LocalDateTime

interface LicenceBookingDetail {
  val licenceVersionId: Long
  val bookingId: Long
  val prisonNumber: String
}

enum class MigrationErrorSource {
  CVL,
  HDC,
}

data class MigrationLicenceVersion(
  val id: Long,
  val prisonNumber: String?,
  val bookingId: Long,
  val version: Int,
  val template: String,
  val varyVersion: Int,
  val deletedAt: LocalDateTime?,
  val licenceInCvl: Boolean,
  val licenceJson: String,
)

@Transactional(propagation = Propagation.NEVER)
@Repository
interface MigrationRepository : CrudRepository<LicenceVersion, Long> {

  @Query(
    value = """
        SELECT 
            lv.id AS id,
            lv.prison_number AS prisonNumber,
            lv.booking_id AS bookingId,
            lv.version AS version,
            lv.template AS template,
            lv.vary_version AS varyVersion,
            lv.deleted_at AS deletedAt,
            lv.licence_in_cvl AS licenceInCvl,
            lv.licence AS licenceJson
        FROM licence_versions lv
            LEFT JOIN (
                    SELECT DISTINCT ON (licence_version_id) licence_version_id, success, retry FROM licence_migration_log ORDER BY licence_version_id, id DESC
            ) migration_log ON migration_log.licence_version_id = lv.id            
            JOIN (    
                SELECT DISTINCT ON (l.booking_id) l.id, l.booking_id FROM licence_versions l
            WHERE l.deleted_at IS NULL
                  AND (l.licence -> 'curfew' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'proposedAddress' -> 'curfewAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'bassOffer' IS NOT NULL)
            ORDER BY l.booking_id, l.version DESC, l.vary_version DESC		    
        ) activeLicence ON activeLicence.id = lv.id 
          WHERE  lv.id = :licenceVersionId AND  (migration_log.licence_version_id IS NULL OR migration_log.retry = true)  
          ORDER BY lv.id
          LIMIT 1
  """,
    nativeQuery = true,
  )
  fun getMigratableLicenceVersion(
    licenceVersionId: Long,
  ): MigrationLicenceVersion?

  @Query(
    value = """
        SELECT 
            lv.id AS id,
            lv.prison_number AS prisonNumber,
            lv.booking_id AS bookingId,
            lv.version AS version,
            lv.template AS template,
            lv.vary_version AS varyVersion,
            lv.deleted_at AS deletedAt,
            lv.licence_in_cvl AS licenceInCvl,
            lv.licence AS licenceJson
        FROM licence_versions lv WHERE  lv.id = :licenceVersionId 
  """,
    nativeQuery = true,
  )
  fun getLicenceVersion(
    licenceVersionId: Long,
  ): MigrationLicenceVersion

  @Query(
    value = """
        SELECT lv.id AS licenceVersionId, lv.booking_id AS bookingId, lv.prison_number AS prisonNumber 
            FROM licence_versions lv
            LEFT JOIN (
                    SELECT DISTINCT ON (licence_version_id) licence_version_id, success, retry FROM licence_migration_log ORDER BY licence_version_id, id DESC
            ) migration_log ON migration_log.licence_version_id = lv.id            
            JOIN (    
                SELECT DISTINCT ON (l.booking_id) l.id, l.booking_id FROM licence_versions l
            WHERE l.deleted_at IS NULL
                  AND (l.licence -> 'curfew' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'proposedAddress' -> 'curfewAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'bassOffer' IS NOT NULL)
            ORDER BY l.booking_id, l.version DESC, l.vary_version DESC		    
        ) activeLicence ON activeLicence.id = lv.id 
          WHERE  lv.id > :lastProcessedId AND  (migration_log.licence_version_id IS NULL OR migration_log.retry = true)  
          ORDER BY lv.id
          LIMIT :batchSize
  """,
    nativeQuery = true,
  )
  fun getMigratableLicenceBatch(
    lastProcessedId: Long,
    batchSize: Int,
  ): List<LicenceBookingDetail>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    value = """
            INSERT INTO licence_migration_log(licence_version_id, booking_id, success, retry, message, error_source)  VALUES (:licenceVersionId,:bookingId,:success,:retry,:message,CAST(:source AS migration_error_source))
        """,
    nativeQuery = true,
  )
  fun insertMigrationLog(licenceVersionId: Long, bookingId: Long, success: Boolean, retry: Boolean, message: String? = null, source: String? = null): Int

  @Query(
    value = """
        SELECT message FROM licence_migration_log WHERE licence_version_id = :licenceVersionId and success = :success and retry = :retry
  """,
    nativeQuery = true,
  )
  fun getMigrationLog(licenceVersionId: Long, success: Boolean, retry: Boolean): String?

  @Query(
    value = """
        SELECT count(*) FROM licence_migration_log
  """,
    nativeQuery = true,
  )
  fun getMigrationLogCount(): Int

  @Query(
    value = """
        SELECT lv.id AS licenceVersionId, lv.booking_id AS bookingId, lv.prison_number AS prisonNumber 
            FROM licence_versions lv
            LEFT JOIN (
                    SELECT DISTINCT ON (licence_version_id) licence_version_id, success, retry FROM licence_migration_log 
                    WHERE  booking_id = :bookingId ORDER BY licence_version_id, id DESC
            ) migration_log ON migration_log.licence_version_id = lv.id            
            JOIN (    
                SELECT DISTINCT ON (l.booking_id) l.id, l.booking_id FROM licence_versions l
            WHERE l.deleted_at IS NULL
                  AND l.booking_id = :bookingId
                  AND (l.licence -> 'curfew' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'approvedPremisesAddress' IS NOT NULL
                   OR  l.licence -> 'proposedAddress' -> 'curfewAddress' IS NOT NULL
                   OR  l.licence -> 'bassReferral' -> 'bassOffer' IS NOT NULL)
            ORDER BY l.booking_id, l.version DESC, l.vary_version DESC		    
        ) activeLicence ON activeLicence.id = lv.id 
          WHERE  (migration_log.licence_version_id IS NULL OR migration_log.retry = true)
          ORDER BY lv.id
  """,
    nativeQuery = true,
  )
  fun getMigratableLicenceDetails(bookingId: Long): LicenceBookingDetail?

  @Query(
    value = """
        SELECT max(additional_conditions_version) FROM licences lv  WHERE lv.booking_id = :bookingId GROUP BY lv.booking_id 
     """,
    nativeQuery = true,
  )
  fun getConditionsVersionFor(bookingId: Long): Int?

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    value = "UPDATE licence_migration_log SET retry = :retry WHERE licence_version_id = :licenceVersionId",
    nativeQuery = true,
  )
  fun updateRetryState(licenceVersionId: Long, retry: Boolean): Int

  @Query(
    value = """
        SELECT 
          id as id,
          licence_version_id as licenceVersionId,
          created_at::timestamp as createdTimeStamp,
          booking_id as bookingId,
          success as success,
          retry as retry,
          message as message,
          error_source::text as errorSource
        FROM licence_migration_log
        WHERE (:licenceVersionId IS NULL OR licence_version_id = :licenceVersionId)
          AND (:bookingId IS NULL OR booking_id = :bookingId)
          AND (:errorSource IS NULL OR error_source = CAST(:errorSource AS migration_error_source))
    """,
    countQuery = """
        SELECT count(*) FROM licence_migration_log
        WHERE (:licenceVersionId IS NULL OR licence_version_id = :licenceVersionId)
          AND (:bookingId IS NULL OR booking_id = :bookingId)
          AND (:errorSource IS NULL OR error_source = CAST(:errorSource AS migration_error_source))
    """,
    nativeQuery = true,
  )
  fun getMigrationLogs(
    licenceVersionId: Long?,
    bookingId: Long?,
    errorSource: String?,
    pageable: Pageable,
  ): Page<LicenceMigrationLogEntryDto>
}
