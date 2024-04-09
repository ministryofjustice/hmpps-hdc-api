package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import java.time.LocalDateTime

@Entity
@Table(name = "licences")
class Licence(
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long? = null,
  var prisonNumber: String?,
  val bookingId: Long,

  val stage: String,
  val version: Int,
  val transitionDate: LocalDateTime,
  val varyVersion: Int,
  val additionalConditionsVersion: Int?,
  val standardConditionsVersion: Int?,
  var deletedAt: LocalDateTime?,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val licence: Map<String, Any>?,
) {

  override fun toString(): String {
    return "Licence(id=$id, prison_number='$prisonNumber', bookingId=$bookingId)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Licence) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }
}
