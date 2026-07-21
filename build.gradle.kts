import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.1"
  id("org.owasp.dependencycheck") version "12.2.2"
  kotlin("plugin.spring") version "2.4.10"
  kotlin("plugin.jpa") version "2.4.10"
  id("dev.detekt") version "2.0.0-alpha.5"
}

repositories {
  mavenCentral()
}

ext["logback.version"] = "1.5.25"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0")

  // Fix for CVE-2025-48924
  implementation("org.apache.commons:commons-lang3:3.20.0")

  // Spring boot dependencies
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-flyway")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.13")

  // SQS/SNS dependencies
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.0")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

  // To help override SAR
  implementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-lib:2.6.2")

  // New in Spring Boot 4: Dedicated starter for HTTP clients
  implementation("org.springframework.boot:spring-boot-starter-webclient")

  // Required for @AutoConfigureWebTestClient and testing WebClient
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")

  // Update to a version compatible with Spring Boot 4.0
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")

  // Test dependencies
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("io.jsonwebtoken:jjwt-api:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.13.0")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("com.h2database:h2")
  testImplementation("org.testcontainers:testcontainers-localstack:2.0.5")
  testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-test-support:2.6.2")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.springframework.boot:spring-boot-webtestclient")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

  // Specifically for Spring Boot 4 Web MVC testing
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

detekt {
  source.setFrom("$projectDir/src/main")
  buildUponDefaultConfig = true // preconfigure defaults
  allRules = false // activate all available (even unstable) rules.
  config.setFrom("$projectDir/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
  baseline = file("$projectDir/detekt-baseline.xml") // a way of suppressing issues before introducing detekt
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(25)) // Java runtime
  }
}

configurations {
  testImplementation {
    exclude(group = "org.junit.vintage")
    exclude(group = "org.mozilla:rhino")
  }

  matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
      if (requested.group == "org.jetbrains.kotlin") {
        useVersion(dev.detekt.gradle.plugin.getSupportedKotlinVersion())
      }
    }
  }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget = JVM_25
      freeCompilerArgs.addAll(
        "-Xwhen-guards",
        "-Xjvm-default=all",
        "-Xjsr305=warn",
        "-Xtype-enhancement-improvements-strict-mode=false",
        "-Xjspecify-annotations=ignore",
      )
    }
  }

  register<Test>("initialiseDatabase") {
    include("**/InitialiseDatabaseTest.class")
  }

  register<Test>("integrationTest") {
    description = "Integration tests"
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    shouldRunAfter("test")
    useJUnitPlatform()

    filter {
      includeTestsMatching("*.integration.*")
    }
  }

  named<Test>("test") {
    filter {
      excludeTestsMatching("*.integration.*")
    }
  }

  register<Copy>("installLocalGitHook") {
    from(File(rootProject.rootDir, ".scripts/pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))
    filePermissions { unix("755") }
  }
  getByName("check") {
    dependsOn(":ktlintCheck", "detekt")
  }
}

allOpen {
  annotation("jakarta.persistence.Entity")
}
