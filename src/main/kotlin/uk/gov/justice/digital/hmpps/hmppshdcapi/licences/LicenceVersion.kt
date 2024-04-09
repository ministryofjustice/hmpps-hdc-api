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
@Table(name = "licence_versions")
class LicenceVersion(
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long? = null,
  var prisonNumber: String?,
  val bookingId: Long,

  val timestamp: LocalDateTime,
  val version: Int,
  val template: String,
  val varyVersion: Int,
  var deletedAt: LocalDateTime?,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val licence: Map<String, Any>?,
) {

  override fun toString(): String {
    return "LicenceVersion(id=$id, prison_number='$prisonNumber', bookingId=$bookingId)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is LicenceVersion) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }
}
