package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.apache.commons.lang3.StringUtils
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.CLOSE
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.CLOSE_STATIC_TEXT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.EXACT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.EXACT_STATIC_TEXT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.NONE
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.PARTIAL
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.MatchType.PARTIAL_STATIC
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V1_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V2_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V2_1
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.POLICY_V3_0
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.IAdditionalCondition
import java.util.Comparator.comparing
import java.util.TreeSet

enum class MatchType(val weight: Int) {
  EXACT_STATIC_TEXT(15), EXACT(12), CLOSE_STATIC_TEXT(9), CLOSE(6), PARTIAL_STATIC(3), PARTIAL(1), NONE(0);

  fun scored(count: Int) = count * weight
}

data class Match(val code: String, val text: String, val distance: Int) : Comparable<Match> {
  override fun compareTo(other: Match) = this.distance.compareTo(other.distance)
}

data class CodeAndText(val code: String, val text: String, val matches: MutableSet<Match> = TreeSet()) {
  fun closestMatch() = matches.first()

  fun placeholderCount() = "<->".toRegex().findAll(text).count()

  fun isStaticText() = placeholderCount() == 0

  fun differenceType(): MatchType {
    val closest = closestMatch()
    return when {
      closest.distance == 0 && isStaticText() -> EXACT_STATIC_TEXT
      closest.distance == 0 -> EXACT
      closest.distance <= 10 && isStaticText() -> CLOSE_STATIC_TEXT
      closest.distance <= 10 -> CLOSE
      closest.distance <= 20 && isStaticText() -> PARTIAL_STATIC
      closest.distance <= 20 -> PARTIAL
      else -> NONE
    }
  }

  override fun toString(): String {
    val closest = closestMatch()
    return "code: $code, placeholderCount: ${placeholderCount()}, text: $text\n  Closest match: [${closest.code}] ${closest.text} (distance: ${closest.distance})\n"
  }
}


data class PolicyDifferences(
  val type: String,
  val hdcVersion: String,
  val cvlVersion: String,
  val hdcConditions: List<CodeAndText>,
  val totalConditionCount: Int = hdcConditions.size,
) {
  val numberOfExactStaticMatches = hdcConditions.count { it.differenceType() == EXACT_STATIC_TEXT }
  val numberOfExactMatches = hdcConditions.count { it.differenceType() == EXACT }
  val numberOfCloseStaticMatches = hdcConditions.count { it.differenceType() == CLOSE_STATIC_TEXT }
  val numberOfCloseMatches = hdcConditions.count { it.differenceType() == CLOSE }
  val numberOfPartialStaticMatches = hdcConditions.count { it.differenceType() == PARTIAL_STATIC }
  val numberOfPartialMatches = hdcConditions.count { it.differenceType() == PARTIAL }
  val numberOfNoMatches = hdcConditions.count { it.differenceType() == NONE }

  val matchScore =
    EXACT_STATIC_TEXT.scored(numberOfExactStaticMatches) + EXACT.scored(numberOfExactMatches) + CLOSE_STATIC_TEXT.scored(
      numberOfCloseStaticMatches,
    ) + CLOSE.scored(numberOfCloseMatches) + PARTIAL_STATIC.scored(numberOfPartialMatches) + PARTIAL.scored(numberOfPartialMatches)

  fun toMap() = mapOf(
    "hdcVersion" to hdcVersion,
    "cvlVersion" to cvlVersion,
    "type" to type,
    "exactStaticMatches" to numberOfExactStaticMatches,
    "exactStaticMatchesPercent" to (numberOfExactStaticMatches / totalConditionCount.toDouble()),
    "exactMatches" to numberOfExactMatches,
    "exactMatchesPercent" to (numberOfExactMatches / totalConditionCount.toDouble()),
    "closeStaticMatches" to numberOfCloseStaticMatches,
    "closeStaticMatchesPercent" to (numberOfCloseStaticMatches / totalConditionCount.toDouble()),
    "closeMatches" to numberOfCloseMatches,
    "closeMatchesPercent" to numberOfCloseMatches / totalConditionCount.toDouble(),
    "partialStaticMatches" to numberOfPartialStaticMatches,
    "partialStaticMatchesPercent" to numberOfPartialStaticMatches / totalConditionCount.toDouble(),
    "partialMatches" to numberOfPartialMatches,
    "partialMatchesPercent" to numberOfPartialMatches / totalConditionCount.toDouble(),
    "noMatch" to numberOfNoMatches,
    "noMatchPercent" to numberOfNoMatches / totalConditionCount.toDouble(),
    "matchScore" to matchScore,
    "totalConditions" to totalConditionCount,
  )

  override fun toString(): String {
    return "CVL Version: $cvlVersion\n" +
      "Exact Matches: $numberOfExactMatches, Close Matches: $numberOfCloseMatches, Partial Matches: $numberOfPartialMatches, No Matches: $numberOfNoMatches\n" +
      "Match Score: $matchScore\n"
  }
}

fun String.replacePlaceholders() = this.replace("\\[.*?]".toRegex(), "<->")

fun compare(
  hdcVersion: String,
  hdc: List<ConditionMetadata>,
  cvlVersion: String,
  cvl: Set<IAdditionalCondition>,
): List<PolicyDifferences> {

  val hdcConditions = hdc.map { CodeAndText(it.id, it.text.replacePlaceholders()) }
  val cvlConditions = cvl.map { CodeAndText(it.code, it.text.replacePlaceholders()) }

  for (hdcCondition in hdcConditions) {
    for (cvlCondition in cvlConditions) {
      val distance = StringUtils.getLevenshteinDistance(hdcCondition.text, cvlCondition.text)
      hdcCondition.matches.add(Match(cvlCondition.code, cvlCondition.text, distance))
    }
  }

  val noInputConditions = hdcConditions.filter { it.isStaticText() }
  return listOf(
    PolicyDifferences("all", hdcVersion, cvlVersion, hdcConditions),
    PolicyDifferences("noInput", hdcVersion, cvlVersion, noInputConditions),
  )
}

fun main() {
  val hdcPolicies = mapOf(
    "v1" to V1_CONDITIONS,
    "v2" to V2_CONDITIONS,
  )

  val cvlPolicies = mapOf(
    "v1.0" to POLICY_V1_0,
    "v2.0" to POLICY_V2_0,
    "v2.1" to POLICY_V2_1,
    "v3.0" to POLICY_V3_0,
  )

  val results = hdcPolicies.flatMap { hdcPolicy ->
    cvlPolicies.flatMap { cvlPolicy ->
      compare(
        hdcPolicy.key,
        hdcPolicy.value,
        cvlPolicy.key,
        cvlPolicy.value.allAdditionalConditions(),
      )
    }
  }

  val policyComparisons =
    results.sortedWith(
      comparing<PolicyDifferences, String> { it.type }.thenComparing { it.hdcVersion }
        .thenComparing { it.cvlVersion },
    ).map { it.toMap() }.joinToString(separator = "\n") { it.values.joinToString() }

  println(policyComparisons)

  val v1 = results.find { it.hdcVersion == "v1" && it.cvlVersion == "v2.0" && it.type == "all" }!!
  printInDetailConditionInfo("v1", v1, CONDITION_FREQUENCIES[1]!!)

  val v2 = results.find { it.hdcVersion == "v2" && it.cvlVersion == "v2.0" && it.type == "all" }!!
  printInDetailConditionInfo("v2", v2, CONDITION_FREQUENCIES[2]!!)
}

private fun printInDetailConditionInfo(version: String, differences: PolicyDifferences, freqs: Map<String, Stats>) {
  println("#### Info for: $version ####")
  val hdcConditions = differences.hdcConditions.sortedWith(
    comparing<CodeAndText, Int> { it.differenceType().weight }.reversed()
      .thenBy { it.closestMatch().distance },
  )

  println("\n** Summary Info for: $version")
  hdcConditions.forEach {
    with(it) {
      val closest = it.closestMatch()
      val freqStats = freqs[code]
      println("$code|${differenceType()}|${isStaticText()}|${closest.distance}|${freqStats?.percentTo2DP}|${freqStats?.num}")
    }
  }

  println("\n** Detailed Info for: $version")

  println("Type|Code|Category|Distance|Count|Freq|Text")
  hdcConditions.forEach {
    with(it) {
      val closest = it.closestMatch()
      val freqStats = freqs[code]
      println("||${differenceType()}|${closest.distance}|${freqStats?.num}|${freqStats?.percentTo2DP}|")
      println("CVL|$code|||||$text")
      println("HDC|${closest.code}|||||${closest.text}")
      println("")
    }
  }

  println("\n** Overall Frequency Info for: $version")
  val total = freqs.values.first().total
  hdcConditions.mapNotNull {
    with(it) {
      val type = differenceType()
      val freqStats = freqs[code]
      freqStats?.let { type to it.num }
    }
  }.fold(HashMap<MatchType, Int>()) { acc, pair ->
    val (type, count) = pair
    acc[type] = (acc[type] ?: 0) + count
    acc
  }.toList().sortedByDescending { it.first.weight }.forEach { (type, count) ->
    println("$type|$count|${"%.2f".format(count / total.toDouble() * 100)}%")
  }
  println()
}