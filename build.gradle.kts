plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.1.2"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"
  id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  // Spring boot dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.11.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.9.0")
  implementation("org.springframework.security:spring-security-config:6.3.2")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.4")

  // SQS/SNS dependencies
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.2.2")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

  // Test dependencies
  testImplementation("org.wiremock:wiremock-standalone:3.10.0")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
  testImplementation("io.jsonwebtoken:jjwt-api:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.12.6")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.0")
  testImplementation("io.swagger.parser.v3:swagger-parser-v2-converter:2.1.24")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("com.h2database:h2")
  testImplementation("org.testcontainers:postgresql:1.20.4")
  testImplementation("org.testcontainers:localstack:1.20.4")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.45.0")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    compilerOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
  }
  withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
      html.required.set(true) // observe findings in your browser with structure and code snippets
    }
  }
  getByName("check") {
    dependsOn(":ktlintCheck", "detekt")
  }
}

val integrationTest = task<Test>("integrationTest") {
  description = "Integration tests"
  group = "verification"
  shouldRunAfter("test")
}

tasks.register<Copy>("installLocalGitHook") {
  from(File(rootProject.rootDir, ".scripts/pre-commit"))
  into(File(rootProject.rootDir, ".git/hooks"))
  filePermissions { unix(755) }
}

tasks.named<Test>("integrationTest") {
  useJUnitPlatform()
  filter {
    includeTestsMatching("*.integration.*")
  }
}

tasks.named<Test>("test") {
  filter {
    excludeTestsMatching("*.integration.*")
  }
}

repositories {
  mavenCentral()
}

detekt {
  source.setFrom("$projectDir/src/main")
  buildUponDefaultConfig = true // preconfigure defaults
  allRules = false // activate all available (even unstable) rules.
  config.setFrom("$projectDir/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
  baseline = file("$projectDir/detekt-baseline.xml") // a way of suppressing issues before introducing detekt
}

allOpen {
  annotation("jakarta.persistence.Entity")
}

configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
    }
  }
}
