package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import io.jsonwebtoken.Jwts
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthHelper(private val keyPair: KeyPair) {

  fun setAuthorisation(user: String = "hmpps-hdc-api-admin", roles: List<String> = listOf()): (HttpHeaders) -> Unit {
    val token = createJwt(subject = user, scope = listOf("read"), expiryTime = Duration.ofHours(1L), roles = roles)
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }

  private fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
  ): String = mutableMapOf<String, Any>()
    .also { subject?.let { subject -> it["user_name"] = subject } }
    .also { it["client_id"] = "hmpps-hdc-api-1" }
    .also { roles?.let { roles -> it["authorities"] = roles } }
    .also { scope?.let { scope -> it["scope"] = scope } }
    .let {
      Jwts.builder()
        .id(jwtId)
        .subject(subject)
        .claims(it.toMap())
        .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
        .signWith(keyPair.private, Jwts.SIG.RS256)
        .compact()
    }
}
