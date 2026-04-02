package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

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
  fun oauthPrisonClient(authorizedClientManager: OAuth2AuthorizedClientManager) = createOauthWebClient(prisonApiUrl, authorizedClientManager)

  @Bean
  fun oauthPrisonerSearchClient(authorizedClientManager: OAuth2AuthorizedClientManager) = createOauthWebClient(prisonerSearchApiUrl, authorizedClientManager)

  @Bean
  fun oauthCvlClient(authorizedClientManager: OAuth2AuthorizedClientManager) = createOauthWebClient(cvlApiUrl, authorizedClientManager)

  private fun createOauthWebClient(
    baseUrl: String,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(HMPPS_AUTH)

    return WebClient.builder()
      .baseUrl(baseUrl)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { it.defaultCodecs().maxInMemorySize(-1) }
          .build(),
      )
      .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
      .build()
  }

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
}
