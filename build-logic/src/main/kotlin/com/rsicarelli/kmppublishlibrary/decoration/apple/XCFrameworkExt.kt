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
     * The resulting path for the XCFramework output is constructed as follows:
     *
     * 1. **Build Directory**: The base directory where all build outputs of the Gradle project
     *    are placed, typically represented by the "build" folder.
     *
     * 2. **"XCFrameworks"**: A standardized subdirectory for XCFramework outputs to keep the
     *    build directory organized. This is a hard-coded value set by the XCFrameworkTask class
     *
     * 3. **[NativeBuildType]**: The build type for which the XCFramework was generated. Its name
     *    is appended in lowercase form for consistency.
     *
     * Example:
     * For a project with a build directory path of "/path/to/project/build" and a
     * [NativeBuildType.DEBUG], the resulting XCFramework path would be
     * "/path/to/project/build/XCFrameworks/debug".
     *
     * @param nativeBuildType The build type for which the XCFramework output path
     *        should be determined.
     * @return The path to the directory containing the XCFramework for the given build type.
     */
    fun Project.getXCFrameworkOutputDirectory(
        nativeBuildType: NativeBuildType,
        xCFrameworkBuildPath: String = "XCFrameworks",
    ): String = "${projectBuildDirectory}/$xCFrameworkBuildPath/${nativeBuildType.getName()}"

    /**
     * Retrieves the name of the Gradle task responsible for assembling the XCFramework
     * for a specific `NativeBuildType`.
     *
     * In Gradle projects that support the creation of XCFramework, there may be various tasks
     * designed to assemble these frameworks based on different configurations or build types.
     * This function aims to dynamically construct the task name for a given build type.
     *
     * The resulting task name is constructed as follows:
     *
     * 1. **"assemble" Prefix**: A common prefix for tasks in Gradle that involve
     *    compilation or assembly of sources into distributable units.
     *
     * 2. **Host Project Name**: The name of the host project (usually the module or
     *    subproject within a larger Gradle project) that's being built. This ensures
     *    that the task is specific to the project in question, especially important in
     *    multi project setups.
     *
     * 3. **[NativeBuildType]**: The build type for which the XCFramework is being assembled.
     *    In multi-platform projects, there could be different build types like "Debug",
     *    "Release", etc., and each might have a different configuration or set of
     *    optimizations.
     *
     * 4. **"XCFramework" Suffix**: A suffix to indicate that this task is specifically
     *    related to XCFrameworks, distinguishing it from other potential assemble tasks
     *    in the project.
     *
     * Example:
     * For a host project named "MyLibrary" and a [NativeBuildType] of [NativeBuildType.DEBUG], the
     * resulting task name would be "assembleMyLibraryDebugXCFramework".
     *
     * @param nativeBuildType The build type for which the XCFramework assembly task name
     *        should be generated.
     * @return The name of the task that assembles the XCFramework for the given build type.
     */
    fun Project.getXCFrameworkAssembleTaskName(
        nativeBuildType: NativeBuildType,
        xCFrameworkSuffix: String = "XCFramework",
    ): String = "assemble${name.capitalized()}${nativeBuildType.getName().capitalized()}$xCFrameworkSuffix"

}
