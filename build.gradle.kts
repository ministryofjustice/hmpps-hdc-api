plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.13.0"
  kotlin("plugin.spring") version "1.9.21"
  kotlin("plugin.jpa") version "1.9.21"
  id("io.gitlab.arturbosch.detekt") version "1.23.4"
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
  implementation("io.opentelemetry:opentelemetry-api:1.34.1")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.0.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.5.1")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.6.0")

  // SQS/SNS dependencies
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.1.1")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

  // Test dependencies
  testImplementation("org.testcontainers:postgresql:1.19.3")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt-api:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.12.3")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.0.0")
  testImplementation("io.swagger.parser.v3:swagger-parser-v2-converter:2.1.16")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("com.h2database:h2")
  testImplementation("org.testcontainers:localstack:1.19.3")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
  withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
      html.required.set(true) // observe findings in your browser with structure and code snippets
    }
  }
  named("check").configure {
    this.setDependsOn(
      this.dependsOn.filterNot {
        it is TaskProvider<*> && it.name == "detekt"
      }
    )
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
  fileMode = "755".toInt(radix = 8)
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
