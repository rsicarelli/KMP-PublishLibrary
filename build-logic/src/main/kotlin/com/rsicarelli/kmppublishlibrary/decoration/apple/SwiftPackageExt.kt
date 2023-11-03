package com.rsicarelli.kmppublishlibrary.decoration.apple

import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageDescriptor.Companion.SWIFT_PACKAGE_FILE_NAME
import com.rsicarelli.kmppublishlibrary.ext.projectBuildDirectory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

internal object SwiftPackageExt {

    /**
     * The default build path for Swift packages. This path is used as a base directory
     * to store and manage Swift packages within the project.
     */
    private const val SWIFT_PACKAGE_BUILD_PATH = "swiftpackages"

    /**
     * Extension property to get the directory for Swift packages builds in the project.
     **/
    internal val Project.swiftPackageBuildDirectory: File
        get() = File(projectBuildDirectory, SWIFT_PACKAGE_BUILD_PATH)

    /**
     * Extension property to get the directory of the Swift package project.
     **/
    internal val swiftPackageProjectDirectory: File
        get() = File(SWIFT_PACKAGE_BUILD_PATH)

    /**
     * Constructs the filename for the Swift package zip archive based on the project's name,
     * the provided [NativeBuildType], and the project's version.
     *
     * @param nativeBuildType The native build type for which the Swift package is intended.
     * @return String representing the Swift package zip filename.
     */
    internal fun Project.getSwiftPackageZipFileName(nativeBuildType: NativeBuildType): String =
        "$name-${nativeBuildType.getName()}-$version.zip"

    /**
     * Computes the checksum of the Swift package zip archive. It ensures the file's existence
     * and then computes the checksum.
     *
     * @param swiftPackageZipFileName The name of the Swift package zip file.
     * @return String representing the computed checksum for the Swift package zip file.
     * @throws FileNotFoundException If the specified Swift package zip file does not exist.
     */
    internal fun Project.computeSwiftPackageChecksum(
        swiftPackageZipFileName: String,
    ): String {
        File(/* parent = */ swiftPackageBuildDirectory,/* child = */ swiftPackageZipFileName).apply {
            if (!exists()) throw FileNotFoundException("XCFramework zip file not found")

            return computeXCFrameworkChecksum(this)
        }
    }

    /**
     * Computes the checksum for an XCFramework file.
     * It expects the checksum output to be non-empty.
     *
     * @param file The XCFramework file for which the checksum needs to be computed.
     * @return String representing the computed checksum for the provided file.
     */
    @Throws(IllegalArgumentException::class)
    private fun Project.computeXCFrameworkChecksum(file: File): String =
        ByteArrayOutputStream().use { outputStream ->
            exec {
                workingDir = file.parentFile
                executable = "swift"
                args = listOf("package", "compute-checksum", name)
                standardOutput = outputStream
            }
            outputStream.toString().trim().also { require(it.isNotEmpty()) }
        }

    /**
     * Creates a `Package.swift` file and returns its [File] reference.
     */
    internal fun Project.generateSwiftPackageFile(): File =
        File(swiftPackageBuildDirectory, SWIFT_PACKAGE_FILE_NAME).apply {
            parentFile?.mkdirs()
            createNewFile()
        }
}
