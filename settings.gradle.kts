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
    plugins {
        id("com.google.devtools.ksp") version "1.9.23-1.0.19"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            // Ensure we can fetch all Google dependencies including Gemini API
            content {
                includeGroup("com.google.ai.client.generativeai")
                includeGroup("com.google.api-client")
                includeGroup("com.google.http-client")
                includeGroup("com.google.apis")
                includeGroup("com.google.auth")
                includeGroup("com.google.android.gms")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "Numl Exam Buddy"
include(":app")
