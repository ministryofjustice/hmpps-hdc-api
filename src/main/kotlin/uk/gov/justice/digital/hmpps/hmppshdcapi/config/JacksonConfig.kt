package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

@Configuration
class JacksonConfig {

  @Bean
  @Primary
  fun objectMapper(): ObjectMapper = customise(JsonMapper.builder()).build()

  @Bean
  fun customizer(): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer { builder ->
    builder?.let { customise(it) }
  }

  private fun customise(builder: JsonMapper.Builder): JsonMapper.Builder = builder
    .findAndAddModules()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    .addModule(
      KotlinModule.Builder()
        .configure(KotlinFeature.StrictNullChecks, false)
        .build(),
    )
}
