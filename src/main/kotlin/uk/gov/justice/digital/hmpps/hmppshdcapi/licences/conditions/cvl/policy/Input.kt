package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.InputType.TEXT

object Fields {
  /**
   * Some fields are only used for display purposes, e.g: expanding conditional inputs.
   * These fields do not contribute to the licence and should not be played back to the user.
   */
  val NON_CONTRIBUTING_FIELDS = setOf("numberOfCurfews", "nameTypeAndOrAddress", "addressOrGeneric")
}

enum class InputType(@JsonValue val description: String) {
  RADIO("radio"),
  ADDRESS("address"),
  TIME_PICKER("timePicker"),
  DATE_PICKER("datePicker"),
  FILE_UPLOAD("fileUpload"),
  TEXT("text"),
  CHECK("check"),
}

data class Input(
  override val type: InputType,
  val label: String,
  val name: String,
  override val listType: String? = null,
  val options: List<Option>? = null,
  override val case: Case? = null,
  override val handleIndefiniteArticle: Boolean? = null,
  val addAnother: AddAnother? = null,
  override val includeBefore: String? = null,
  val subtext: String? = null,
) : FormattingRule {

  @JsonIgnore
  fun getAllFieldNames(): List<String> {
    val conditionalInputFields: List<String> = options?.mapNotNull {
      it.conditional?.inputs?.map { c -> c.name }
    }?.flatten() ?: emptyList()
    return if (conditionalInputFields.isNotEmpty()) {
      conditionalInputFields + name
    } else {
      listOf(name)
    }
  }
}

interface FormattingRule {
  val type: InputType
  val case: Case?
  val listType: String?
  val includeBefore: String?
  val handleIndefiniteArticle: Boolean?

  companion object {
    val DEFAULT = object : FormattingRule {
      override val type = TEXT
      override val case = null
      override val listType = "AND"
      override val includeBefore = null
      override val handleIndefiniteArticle = null
    }
  }
}

data class AddAnother(
  val label: String,
)
