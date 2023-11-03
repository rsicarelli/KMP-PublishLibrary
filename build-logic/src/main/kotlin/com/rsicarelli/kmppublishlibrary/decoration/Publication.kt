package com.rsicarelli.kmppublishlibrary.decoration

import com.rsicarelli.kmppublishlibrary.decoration.PublicationDefaults.SWIFT_PACKAGE_TASK_GROUP
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageDescriptor.LocalSwiftPackageDescriptor.Companion.localSwiftPackageDescriptor
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageDescriptor.RemoteSwiftPackageDescriptor.Companion.remoteSwiftPackageDescriptor
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageExt.computeSwiftPackageChecksum
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageExt.generateSwiftPackageFile
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageExt.getSwiftPackageZipFileName
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageExt.swiftPackageBuildDirectory
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageExt.swiftPackageProjectDirectory
import com.rsicarelli.kmppublishlibrary.decoration.apple.XCFrameworkExt.getXCFrameworkAssembleTaskName
import com.rsicarelli.kmppublishlibrary.decoration.apple.XCFrameworkExt.getXCFrameworkOutputDirectory
import com.rsicarelli.kmppublishlibrary.options.LibraryPublicationOptions
import com.rsicarelli.kmppublishlibrary.options.SwiftPackageOptions
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

internal fun Project.applyMultiplatformLibraryPublication(
    options: LibraryPublicationOptions,
) {
    applyNativeLibraryPublication(options.swiftPackagesOptions)
}

/**
 * Applies native library publication configurations to the project based on the provided
 * [SwiftPackageOptions].
 *
 * @param options Contains the settings and preferences for the Swift package publication.
 */
private fun Project.applyNativeLibraryPublication(options: SwiftPackageOptions) {
    NativeBuildType.DEFAULT_BUILD_TYPES
        .forEach { nativeBuildType ->
            registerCopyXCFrameworkTask(nativeBuildType)
            registerZipXCFrameworkTask(nativeBuildType)
            registerCreateLocalSwiftPackageTask(nativeBuildType, options)
            registerPublishRemoteSwiftPackageTask(nativeBuildType, options)
        }
}

/**
 * Registers a task for publishing a remote Swift package.
 *
 * This task is primarily responsible for creating a Swift package designed for
 * remote distribution.
 *
 * @param nativeBuildType The specific native build type to target (e.g., Debug, Release).
 * @param options Configurations and parameters related to the Swift package. These can include details
 *                like package metadata, destination URL, authentication credentials, and more.
 * @see NativeBuildType
 */
private fun Project.registerPublishRemoteSwiftPackageTask(
    nativeBuildType: NativeBuildType,
    options: SwiftPackageOptions,
) {
    tasks.register(nativeBuildType.publishRemoteTaskName) {
        group = SWIFT_PACKAGE_TASK_GROUP
        description = "Creates the Swift package to distribute the XCFramework"

        dependsOn(nativeBuildType.zipTaskName)

        doLast {
            //todo upload cloud

            generateRemoteSwiftPackageFile(nativeBuildType, options)
        }
    }
}

/**
 * Generates a `Package.swift` file for a remote package and writes its content.
 */
private fun Project.generateRemoteSwiftPackageFile(
    nativeBuildType: NativeBuildType,
    options: SwiftPackageOptions,
): Unit =
    remoteSwiftPackageDescriptor {
        val packageZipFileName: String = getSwiftPackageZipFileName(nativeBuildType)

        packageName = name
        zipFileName = packageZipFileName
        zipChecksum = computeSwiftPackageChecksum(packageZipFileName)
        distributionUrl = options.swiftPackageDistributionURL
        platforms = options.appleTargetOptions.getAsPlatformVersions()
        swiftVersion = options.swiftVersion
    }.writeTo(
        writer = generateSwiftPackageFile().writer()
    )

/**
 * Registers a task for creating a local Swift package.
 *
 * The purpose of this task is to construct a Swift package suitable for local consumption.
 * In a typical scenario, this allows developers to test and integrate the XCFramework
 * into a Swift project without the need to publish it to a remote server or repository.
 *
 * Developers can use this task to facilitate local testing and integration of the
 * XCFramework by quickly generating a consumable Swift package within the project's ecosystem.
 *
 * @param nativeBuildType The specific native build type being targeted (e.g., Debug, Release).
 */
private fun Project.registerCreateLocalSwiftPackageTask(
    nativeBuildType: NativeBuildType,
    swiftPackageOptions: SwiftPackageOptions,
) {
    tasks.register(nativeBuildType.createLocalTaskName) {
        group = SWIFT_PACKAGE_TASK_GROUP
        description = "Creates a local Swift package to distribute the XCFramework"

        dependsOn(nativeBuildType.zipTaskName)
        finalizedBy(nativeBuildType.copyTaskName)

        doLast {
            generateLocalPackageSwiftFile(nativeBuildType, swiftPackageOptions)
        }
    }
}

/**
 * Generates a `Package.swift` file for a local package and writes its content.
 */
private fun Project.generateLocalPackageSwiftFile(
    nativeBuildType: NativeBuildType,
    swiftPackageOptions: SwiftPackageOptions,
) = localSwiftPackageDescriptor {
    packageName = name
    zipFileName = getSwiftPackageZipFileName(nativeBuildType)
    platforms = swiftPackageOptions.appleTargetOptions.getAsPlatformVersions()
    swiftVersion = swiftPackageOptions.swiftVersion
}.writeTo(generateSwiftPackageFile().writer())

/**
 * Registers a task for zipping the XCFramework.
 *
 * In Apple development, XCFrameworks are binary packages that bundle together
 * multiple versions of a framework for different platforms and architectures,
 * ensuring that developers can integrate them into apps across the Apple ecosystem.
 *
 * The purpose of this task is to provide a compressed (ZIP) version of the XCFramework,
 * which makes it easier to distribute, especially in contexts where developers are
 * looking to publish or share the framework.
 *
 * @param nativeBuildType The specific native build type being targeted (e.g., Debug, Release).
 */
private fun Project.registerZipXCFrameworkTask(
    nativeBuildType: NativeBuildType,
) {
    tasks.register(nativeBuildType.zipTaskName, Zip::class) {
        group = SWIFT_PACKAGE_TASK_GROUP
        description = "Creates a ZIP file for the XCFramework"

        dependsOn(getXCFrameworkAssembleTaskName(nativeBuildType))

        archiveFileName.set(getSwiftPackageZipFileName(nativeBuildType))
        destinationDirectory.set(swiftPackageBuildDirectory)
        from(getXCFrameworkOutputDirectory(nativeBuildType))
    }
}

/**
 * Registers a task for copying the XCFramework to the Swift Package output directory.
 *
 * @param nativeBuildType The specific native build type in focus (e.g., Debug, Release).
 */
private fun Project.registerCopyXCFrameworkTask(
    nativeBuildType: NativeBuildType,
) {
    tasks.register(nativeBuildType.copyTaskName, Copy::class) {
        group = SWIFT_PACKAGE_TASK_GROUP
        description = "Copy the XCFramework into Swift Package output directory"

        dependsOn(getXCFrameworkAssembleTaskName(nativeBuildType))

        from(swiftPackageBuildDirectory)
        into(swiftPackageProjectDirectory)
    }
}

/** Extension to generate the task name for zipping XCFramework based on the native build type. */
private val NativeBuildType.zipTaskName: String
    get() = "zip${getName().capitalized()}XCFramework"

/** Extension to generate the task name for copying XCFramework based on the native build type. */
private val NativeBuildType.copyTaskName: String
    get() = "copy${getName().capitalized()}XCFramework"

/** Extension to generate the task name for creating a local Swift package based on the native build type. */
private val NativeBuildType.createLocalTaskName: String
    get() = "createLocal${getName().capitalized()}SwiftPackage"

/** Extension to generate the task name for publishing a remote Swift package based on the native build type. */
private val NativeBuildType.publishRemoteTaskName: String
    get() = "publishRemote${getName().capitalized()}SwiftPackage"

private object PublicationDefaults {

    /**
     * A constant used to categorize and organize specific tasks under the "Swift Package"
     * section within the Gradle task list.
     */
    const val SWIFT_PACKAGE_TASK_GROUP = "Swift Package"
}
