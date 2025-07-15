pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {

            url = uri("https://maven.pkg.github.com/fastpix-gopi/android-data-androidXmedia3")

        }
        maven {
            url =uri("https://muxinc.jfrog.io/artifactory/default-maven-release-local")
        }
    }
}

rootProject.name = "AndroidFastpixPlayer"
include(":app")
include(":library")
