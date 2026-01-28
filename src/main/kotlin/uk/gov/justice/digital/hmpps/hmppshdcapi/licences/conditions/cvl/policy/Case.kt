package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy

import com.fasterxml.jackson.annotation.JsonValue

enum class Case(@JsonValue val description: String) {
  LOWER("lower"),
  UPPER("upper"),
  CAPITALISED("capitalised"),
}
