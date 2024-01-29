package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
