package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.util.PropertySource
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.CLOSE
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.CLOSE_STATIC_TEXT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.EXACT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.EXACT_STATIC_TEXT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.NONE
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.PARTIAL
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V1_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V2_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V2_1
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V3_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.IAdditionalCondition
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Comparator.comparing
import java.util.TreeSet
import kotlin.math.roundToInt
import kotlin.math.round

/**
 * <pre>
select
additional_conditions_version,
jsonb_object_keys(licence -> 'licenceConditions' -> 'additional'),
count(*)
from
licences l
where
licence -> 'licenceConditions' -> 'additional' is not null
and deleted_at is null
group by
jsonb_object_keys(licence -> 'licenceConditions' -> 'additional'),
additional_conditions_version
order by additional_conditions_version, jsonb_object_keys(licence -> 'licenceConditions' -> 'additional');
</pre>
 */

data class Stats(val num: Int, val total: Int) {
  val percent: Double get() = (num / total.toDouble() * 100)
  val percentTo2DP: String get() = "%.2f".format(percent)
}

val CONDITION_FREQUENCIES =
  File("src/main/kotlin/uk/gov/justice/digital/hmpps/hmppshdcapi/licences/conditions/frequencies.psv").readLines(
    UTF_8,
  ).drop(2).map { it.split("|").map { it.trim() }.dropLast(1) }
    .groupBy({ it[0].toInt() }, { it[1] to it[2].toInt() })
    .mapValues { (version, conditions) ->
      val total = conditions.sumOf { it.second }
      conditions.associate { (k, v) -> k to Stats(v, total) }.toMap()
    }
