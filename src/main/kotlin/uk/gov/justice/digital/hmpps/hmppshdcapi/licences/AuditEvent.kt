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
@Table(name = "audit")
class AuditEvent(
  @Id
  @param:NotNull
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  val id: Long? = null,
  var timestamp: LocalDateTime?,
  @Column(name = "`user`", nullable = false)
  var user: String,
  var action: String,

  @JdbcTypeCode(SqlTypes.JSON)
  val details: Map<String, Any>,
) {

  override fun toString(): String = "Audit(id=$id, timestamp='$timestamp', user=$user, action=$action, details= $details)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AuditEvent) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int = id?.hashCode() ?: 0
}
