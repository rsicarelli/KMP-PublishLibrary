package com.rsicarelli.kmppublishlibrary.ext

import java.io.File
import org.gradle.api.Project

/**
 * Extension property to get the build directory for the project.
 *
 * @return File representing the root build directory for the project.
 */
val Project.projectBuildDirectory: File
    get() = layout.buildDirectory.asFile.get()
