package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import java.io.File

private fun RequestMappingInfo.getMappings() = methodsCondition.methods.map { it.name }.flatMap { method ->
  pathPatternsCondition!!.patternValues.map { "$method $it" }
}

class ResourceSecurityTest : SqsIntegrationTestBase() {
  @Autowired
  private lateinit var context: ApplicationContext

  private val unprotectedDefaultMethods = setOf(
    "GET /v3/api-docs.yaml",
    "GET /swagger-ui.html",
    "GET /v3/api-docs",
    "GET /v3/api-docs/swagger-config",
    "/error",
    "PUT /queue-admin/retry-all-dlqs",
    "POST /jobs/delete-inactive-licences",
  )

  @Test
  fun `Ensure all endpoints protected with PreAuthorize`() {
    // need to exclude any that are forbidden in helm configuration
    val exclusions = File("helm_deploy").walk().filter { it.name.equals("values.yaml") }.flatMap { file ->
      file.readLines().map { line ->
        line.takeIf { it.contains("location") }?.substringAfter("location ")?.substringBefore(" {")
      }
    }.filterNotNull().flatMap { path -> listOf("GET", "POST", "PUT", "DELETE").map { "$it $path" } }
      .toMutableSet().also {
        it.addAll(unprotectedDefaultMethods)
      }

    context.getBeansOfType(RequestMappingHandlerMapping::class.java).forEach { (_, mapping) ->
      mapping.handlerMethods.forEach { (mappingInfo, method) ->
        val classAnnotation = method.beanType.getAnnotation(PreAuthorize::class.java)
        val annotation = method.getMethodAnnotation(PreAuthorize::class.java)
        if (classAnnotation == null && annotation == null) {
          mappingInfo.getMappings().forEach {
            Assertions.assertThat(exclusions.contains(it)).withFailMessage {
              "Found $mappingInfo of type $method with no PreAuthorize annotation"
            }.isTrue()
          }
        }
      }
    }
  }
}
