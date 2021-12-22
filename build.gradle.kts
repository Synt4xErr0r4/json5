import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  jacoco
  `java-library`
  id("me.qoomon.git-versioning") version "5.1.1"
  `maven-publish`
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  val kotlinxSerializationVersion = "1.3.1"
  implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinxSerializationVersion"))
  api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

  val junitVersion = "5.8.2"
  testImplementation(platform("org.junit:junit-bom:$junitVersion"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
    because("Only needed to run tests in a version of IntelliJ IDEA that bundles older versions")
  }

  val kotestVersion = "5.0.3"
  testImplementation(platform("io.kotest:kotest-bom:$kotestVersion"))
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
}

group = "at.syntaxerror"
description = "JSON5 for Kotlin"
version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
  refs {
    branch(".+") { version = "\${ref}-SNAPSHOT" }
    tag("v(?<version>.*)") { version = "\${ref.version}" }
  }

  // optional fallback configuration in case of no matching ref configuration
  rev { version = "\${commit}" }
}

java {
  withJavadocJar()
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

tasks.wrapper {
  gradleVersion = "7.3.3"
  distributionType = Wrapper.DistributionType.ALL
}
tasks.assemble { dependsOn(tasks.wrapper) }

tasks.javadoc {
  if (JavaVersion.current().isJava9Compatible) {
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}
