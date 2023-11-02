import org.gradle.api.internal.FeaturePreviews

enableFeaturePreview(FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS.toString())
enableFeaturePreview(FeaturePreviews.Feature.STABLE_CONFIGURATION_CACHE.toString())

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KMP-publish-library"

include("library")
