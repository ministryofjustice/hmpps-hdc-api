package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient

private const val HMPPS_AUTH = "hmpps-auth"
private const val MAX_IN_MEMORY_SIZE = 10485760 // 10 MB

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps.auth.url}") private val oauthApiUrl: String,
  @param:Value("\${hmpps.prison.api.url}") private val prisonApiUrl: String,
  @param:Value("\${hmpps.prisonersearch.api.url}") private val prisonerSearchApiUrl: String,
  @param:Value("\${hmpps.cvl.api.url}") private val cvlApiUrl: String,
) {

  @Bean
  fun oauthApiHealthWebClient(): WebClient = WebClient.builder()
    .baseUrl(oauthApiUrl)
    .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
    .build()

  @Bean
  fun oauthPrisonClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder) = getWebClient(prisonApiUrl, authorizedClientManager, builder)

  @Bean
  fun oauthPrisonerSearchClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder) = getWebClient(prisonerSearchApiUrl, authorizedClientManager, builder)

  @Bean
  fun oauthCvlClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ) = getWebClient(
    cvlApiUrl,
    authorizedClientManager,
    builder,
  )

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider =
      OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()

    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    ).apply { setAuthorizedClientProvider(authorizedClientProvider) }
  }

  private fun getWebClient(
    url: String,
    authorizedClientManagerCvl: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
    maxInMemorySize: Int = MAX_IN_MEMORY_SIZE,
  ): WebClient = builder
    .authorisedWebClient(
      authorizedClientManager = authorizedClientManagerCvl,
      url = url,
      registrationId = HMPPS_AUTH,
    )
    .mutate()
    .codecs { it.defaultCodecs().maxInMemorySize(maxInMemorySize) }
    .build()
}
