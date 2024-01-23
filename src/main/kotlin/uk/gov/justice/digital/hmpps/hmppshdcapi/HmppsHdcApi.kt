package uk.gov.justice.digital.hmpps.hmppshdcapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmppsHdcApi

fun main(args: Array<String>) {
  runApplication<HmppsHdcApi>(*args)
}
