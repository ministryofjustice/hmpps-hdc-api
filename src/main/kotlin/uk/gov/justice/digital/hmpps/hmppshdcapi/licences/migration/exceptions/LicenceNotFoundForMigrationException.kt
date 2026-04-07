package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions

class LicenceNotFoundForMigrationException(id: Long) : Exception("Licence $id not found for migration")
