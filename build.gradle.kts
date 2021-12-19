import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "1.6.10"
  kotlin("jvm") version kotlinVersion
  jacoco
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  val kotlinxSerializationVersion = "1.3.1"
  implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinxSerializationVersion"))
  api("org.jetbrains.kotlinx:kotlinx-serialization-core")
  api("org.jetbrains.kotlinx:kotlinx-serialization-json")

  val junitVersion = "5.8.2"
  testImplementation(enforcedPlatform("org.junit:junit-bom:$junitVersion"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
    because("Only needed to run tests in a version of IntelliJ IDEA that bundles older versions")
  }

  val kotestVersion = "5.0.2"
  testImplementation(enforcedPlatform("io.kotest:kotest-bom:$kotestVersion"))
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")

  testImplementation("io.mockk:mockk:1.12.1")
}

group = "at.syntaxerror"
version = "2.0.0"
description = "JSON5 for Kotlin"

java {
  withSourcesJar()
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<KotlinCompile>().configureEach {

  kotlinOptions {
    jvmTarget = "11"
    apiVersion = "1.6"
    languageVersion = "1.6"
  }

  kotlinOptions.freeCompilerArgs += listOf(
    "-Xopt-in=kotlin.RequiresOptIn",
    "-Xopt-in=kotlin.ExperimentalStdlibApi",
    "-Xopt-in=kotlin.time.ExperimentalTime",
    "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
  )
}

tasks.withType<Test> {
  useJUnitPlatform()
  // report is always generated after tests run
  finalizedBy(tasks.withType<JacocoReport>())
}

jacoco {
  toolVersion = "0.8.7"
}

tasks.withType<JacocoReport> {
  dependsOn(tasks.withType<Test>())

  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }

  doLast {
    val htmlReportLocation = reports.html.outputLocation.locationOnly
      .map { it.asFile.resolve("index.html").invariantSeparatorsPath }

    logger.lifecycle("Jacoco report for ${project.name}: ${htmlReportLocation.get()}")
  }
}
tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}
