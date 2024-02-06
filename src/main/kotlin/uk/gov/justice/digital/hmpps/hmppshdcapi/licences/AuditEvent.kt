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
@Table(name = "audit")
class AuditEvent(
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long? = null,
  var timestamp: LocalDateTime,
  var user: String,
  var action: String,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val details: Map<String, Any>,
) {

  override fun toString(): String {
    return "Audit(id=$id, timestamp='$timestamp', user=$user, action=$action, details= $details)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AuditEvent) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }
}
