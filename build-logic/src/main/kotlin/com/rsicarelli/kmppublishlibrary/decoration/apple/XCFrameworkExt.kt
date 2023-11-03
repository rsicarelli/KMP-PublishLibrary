package com.rsicarelli.kmppublishlibrary.decoration.apple

import com.rsicarelli.kmppublishlibrary.ext.projectBuildDirectory
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

internal object XCFrameworkExt {

    /**
     * Determines the output directory for the generated XCFramework based on the given
     * [NativeBuildType].
     *
     * Example:
     * For a project with a build directory path of `/path/to/project/build` and a
     * [NativeBuildType.DEBUG], the resulting XCFramework path would be
     * `/path/to/project/build/XCFrameworks/debug`.
     *
     * @return The path to the directory containing the XCFramework for the given build type.
     */
    fun Project.getXCFrameworkOutputDirectory(
        nativeBuildType: NativeBuildType,
        xCFrameworkBuildPath: String = "XCFrameworks",
    ): String = "${projectBuildDirectory}/$xCFrameworkBuildPath/${nativeBuildType.getName()}"

    /**
     * Retrieves the name of the Gradle task responsible for assembling the XCFramework
     * for a specific [NativeBuildType].
     *
     * Example:
     * For a host project named "MyLibrary" and a [NativeBuildType] of [NativeBuildType.DEBUG], the
     * resulting task name would be "assembleMyLibraryDebugXCFramework".
     *
     * @return The name of the task that assembles the XCFramework for the given build type.
     */
    fun Project.getXCFrameworkAssembleTaskName(
        nativeBuildType: NativeBuildType,
        xCFrameworkSuffix: String = "XCFramework",
    ): String = "assemble${name.capitalized()}${nativeBuildType.getName().capitalized()}$xCFrameworkSuffix"

}
