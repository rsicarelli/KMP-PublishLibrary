package com.rsicarelli.kmppublishlibrary.ext

import java.io.File
import org.gradle.api.Project

/**
 * Extension property to get the build directory for the project.
 *
 * The build directory typically contains all the output files from the build process. It's a
 * standard directory in Gradle-based projects where compiled classes, generated sources,
 * processed resources, and other build artifacts are stored.
 *
 * @return File representing the root build directory for the project.
 */
val Project.projectBuildDirectory: File
    get() = layout.buildDirectory.asFile.get()
