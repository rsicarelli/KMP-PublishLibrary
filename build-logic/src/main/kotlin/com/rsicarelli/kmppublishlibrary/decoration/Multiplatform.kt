package com.rsicarelli.kmppublishlibrary.decoration

import com.android.build.gradle.LibraryExtension
import com.rsicarelli.kmppublishlibrary.decoration.apple.ApplePlatformSettings
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig

internal fun Project.applyMultiplatformLibrary(
    appleTargetOptions: ApplePlatformSettings,
) {
    extensions.configure<LibraryExtension> {
        compileSdk = 34
        this.sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        defaultConfig {
            minSdk = 21
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        namespace = "com.rsicarelli.kmp-library-publish"
    }

    extensions.configure<KotlinMultiplatformExtension> {
        applyDefaultHierarchyTemplate()

        androidTarget()
        jvm()

        js(IR) {
            binaries.library()
            useCommonJs()
            browser()
        }

        val xcFrameworkConfig: XCFrameworkConfig = XCFramework()

        appleTargetOptions.toKotlinNativeTargets(
            presetFunctions = this as KotlinTargetContainerWithPresetFunctions
        ).forEach {
            it.binaries.framework {
                baseName = this@applyMultiplatformLibrary.name.lowercase()
                xcFrameworkConfig.add(this)
            }
        }
    }
}
