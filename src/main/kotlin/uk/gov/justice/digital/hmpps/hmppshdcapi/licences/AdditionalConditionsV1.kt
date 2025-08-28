package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

// NOTE: DO NOT EDIT DIRECTLY: This is generated via "npm run generate:kotlin-types" in https://github.com/ministryofjustice/licences 

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
class AdditionalConditionsV1 (
    
    @field:JsonProperty("NOCONTACTPRISONER")
    val nocontactprisoner: Any?,

    @field:JsonProperty("NOCONTACTASSOCIATE")
    val nocontactassociate: Any?,

    @field:JsonProperty("NOCONTACTSEXOFFENDER")
    val nocontactsexoffender: Any?,

    @field:JsonProperty("INTIMATERELATIONSHIP")
    val intimaterelationship: Any?,

    @field:JsonProperty("NOCONTACTNAMED")
    val nocontactnamed: Any?,

    @field:JsonProperty("NORESIDE")
    val noreside: Any?,

    @field:JsonProperty("NOUNSUPERVISEDCONTACT")
    val nounsupervisedcontact: Any?,

    @field:JsonProperty("NOCHILDRENSAREA")
    val nochildrensarea: Any?,

    @field:JsonProperty("NOWORKWITHAGE")
    val noworkwithage: Any?,

    @field:JsonProperty("NOTIFYRELATIONSHIP")
    val notifyrelationship: Any?,

    @field:JsonProperty("NOCOMMUNICATEVICTIM")
    val nocommunicatevictim: Any?,

    @field:JsonProperty("ATTENDDEPENDENCYINDRUGSSECTION")
    val attenddependencyindrugssection: Any?,

    @field:JsonProperty("COMPLYREQUIREMENTS")
    val complyrequirements: Any?,

    @field:JsonProperty("ATTENDALL")
    val attendall: Any?,

    @field:JsonProperty("HOMEVISITS")
    val homevisits: Any?,

    @field:JsonProperty("REMAINADDRESS")
    val remainaddress: Any?,

    @field:JsonProperty("CONFINEADDRESS")
    val confineaddress: Any?,

    @field:JsonProperty("REPORTTO")
    val reportto: Any?,

    @field:JsonProperty("RETURNTOUK")
    val returntouk: Any?,

    @field:JsonProperty("NOTIFYPASSPORT")
    val notifypassport: Any?,

    @field:JsonProperty("SURRENDERPASSPORT")
    val surrenderpassport: Any?,

    @field:JsonProperty("VEHICLEDETAILS")
    val vehicledetails: Any?,

    @field:JsonProperty("EXCLUSIONADDRESS")
    val exclusionaddress: Any?,

    @field:JsonProperty("EXCLUSIONAREA")
    val exclusionarea: Any?,

    @field:JsonProperty("ONEPHONE")
    val onephone: Any?,

    @field:JsonProperty("NOINTERNET")
    val nointernet: Any?,

    @field:JsonProperty("USAGEHISTORY")
    val usagehistory: Any?,

    @field:JsonProperty("NOCAMERA")
    val nocamera: Any?,

    @field:JsonProperty("CAMERAAPPROVAL")
    val cameraapproval: Any?,

    @field:JsonProperty("NOCAMERAPHONE")
    val nocameraphone: Any?,

    @field:JsonProperty("POLYGRAPH")
    val polygraph: Any?,

    @field:JsonProperty("DRUG_TESTING")
    val drugTesting: Any?,

    @field:JsonProperty("ATTENDSAMPLE")
    val attendsample: Any?,

    @field:JsonProperty("ATTENDDEPENDENCY")
    val attenddependency: Any?,
  ): AdditionalConditions
