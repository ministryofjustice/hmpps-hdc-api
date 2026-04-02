package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence

@Repository
interface MigrationRepository : CrudRepository<Licence, Long> {

  @Modifying
  @Transactional
  @Query(
    value = """
            INSERT INTO licence_migration_log(licence_id)  VALUES (:licenceId)
        """,
    nativeQuery = true,
  )
  fun insertMigrationLog(licenceId: Long): Int
}
