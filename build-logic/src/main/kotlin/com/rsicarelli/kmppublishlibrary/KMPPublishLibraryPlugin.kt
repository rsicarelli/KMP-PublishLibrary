/*
 * Copyright (c) Rodrigo Sicarelli 2023.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.rsicarelli.kmppublishlibrary

import com.rsicarelli.kmppublishlibrary.decoration.applyMultiplatformLibrary
import com.rsicarelli.kmppublishlibrary.decoration.applyMultiplatformLibraryPublication
import com.rsicarelli.kmppublishlibrary.decoration.apple.ApplePlatformSettings
import com.rsicarelli.kmppublishlibrary.decoration.apple.AppleVersion
import com.rsicarelli.kmppublishlibrary.options.LibraryPublicationOptions
import com.rsicarelli.kmppublishlibrary.options.SwiftPackageOptions
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Represents a platform plugin for a Gradle project.
 *
 * By applying this plugin, developers can leverage a suite of predefined configurations, often referred to as
 * "decorations," to customize and extend their project's behavior.
 *
 */
class KMPPublishLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) = Unit
}

fun Project.multiplatformLibrary(
    appleTargetOptions: ApplePlatformSettings = ApplePlatformSettings.default,
) {
    applyMultiplatformLibrary(appleTargetOptions)
}

fun Project.multiplatformLibraryPublication(
    appleTargetOptions: ApplePlatformSettings = ApplePlatformSettings.default,
    swift: AppleVersion.Swift = AppleVersion.Swift("5.9"),
) {
    applyMultiplatformLibraryPublication(
        options = LibraryPublicationOptions(
            swiftPackagesOptions = SwiftPackageOptions(
                appleTargetOptions = appleTargetOptions,
                swiftVersion = swift
            )
        )
    )
}
