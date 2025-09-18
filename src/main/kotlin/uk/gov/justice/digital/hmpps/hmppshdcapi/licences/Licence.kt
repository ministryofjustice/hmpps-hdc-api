package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "licences")
class Licence(
  @Id
  @param:NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long? = null,
  var prisonNumber: String,
  val bookingId: Long,

  val stage: String,
  val version: Int,
  val transitionDate: LocalDateTime?,
  val varyVersion: Int,
  val additionalConditionsVersion: Int?,
  val standardConditionsVersion: Int?,
  var deletedAt: LocalDateTime?,
  var licenceInCvl: Boolean,

  @JdbcTypeCode(SqlTypes.JSON)
  val licence: LicenceData?,
) {

  override fun toString(): String = "Licence(id=$id, prison_number='$prisonNumber', bookingId=$bookingId)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Licence) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int = id?.hashCode() ?: 0
}
