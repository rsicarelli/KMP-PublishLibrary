/*
 * Copyright (c) Rodrigo Sicarelli 2023.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    java
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.kotlin)
}

gradlePlugin {
    plugins.register("kplatformPlugin") {
        id = "com.rsicarelli.kmp-publish-library"
        implementationClass = "com.rsicarelli.kmppublishlibrary.KMPPublishLibraryPlugin"
    }
}
