package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.cfg.AvailableSettings
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HibernateConfig {
  @Bean
  fun jsonFormatMapperCustomizer(objectMapper: ObjectMapper?): HibernatePropertiesCustomizer {
    log.info("Registering hibernate config")
    return HibernatePropertiesCustomizer { properties ->
      properties[AvailableSettings.JSON_FORMAT_MAPPER] = JacksonJsonFormatMapper(objectMapper)
    }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}