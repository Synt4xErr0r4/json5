rootProject.name = "json5-kotlin"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {

  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://jitpack.io")
  }

  pluginManagement {
    repositories {
      gradlePluginPortal()
      mavenCentral()
      maven("https://jitpack.io")
    }
  }
}
