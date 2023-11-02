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
 * This function operates over the [NativeBuildType.DEFAULT_BUILD_TYPES], which represents
 * a set of predefined native build types, such as [NativeBuildType.DEBUG] and [NativeBuildType.RELEASE].
 *
 * For each of these build types, the function registers a series of tasks to:
 * 1. Copy the XCFramework.
 * 2. Zip the XCFramework.
 * 3. Create a local Swift package for the XCFramework.
 * 4. Publish a remote Swift package for the XCFramework.
 *
 * The objective is to automate and standardize the process of preparing and distributing
 * XCFramework outputs for different build configurations.
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
 * remote distribution. It follows these main steps:
 *
 * 1. **Dependency Declaration**:
 *      - It first declares a dependency on the associated ZIP task for the given native build type.
 *        This ensures that the ZIP file of the XCFramework is prepared before this task runs.
 *
 * 2. **Execution**:
 *      - Upon execution (`doLast`), the task intends to upload the package to a remote location
 *        (as indicated by the placeholder comment "todo upload cloud").
 *      - Post that, it invokes the `publishRemoteSwiftPackage` function, passing the
 *        native build type and package options, which handles the specifics of the remote publication.
 *
 * Ideally, the "todo upload cloud" comment implies that additional steps or functions are needed to
 * handle the actual upload process to a cloud or remote server. This could involve uploading the ZIP
 * to a package registry, CDN, or some other form of remote storage.
 *
 * @param nativeBuildType The specific native build type to target (e.g., Debug, Release).
 * @param options Configurations and parameters related to the Swift package. These can include details
 *                like package metadata, destination URL, authentication credentials, and more.
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
 * The steps involved in the process are:
 *
 * 1. **Dependency Declaration**:
 *      - Before this task runs, it ensures that the ZIP file of the XCFramework, corresponding
 *        to the given native build type, has been generated. This is achieved by setting a
 *        dependency on the appropriate ZIP task.
 *
 * 2. **Execution**:
 *      - The task's execution (`doLast`) focuses on the local Swift package creation. This is done
 *        by invoking the `createLocalSwiftPackage` function with the specific native build type.
 *
 * 3. **Finalizing Steps**:
 *      - After the local Swift package has been created, this task ensures that the associated
 *        copy task (to move the XCFramework into the Swift Package output directory) is run. This
 *        step helps in keeping the local Swift package and the XCFramework in sync.
 *
 * In essence, developers can use this task to facilitate local testing and integration of the
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
 * The purpose of this task is to provide a compressed (ZIP) version of the XCFramework,
 * which makes it easier to distribute, especially in contexts where developers are
 * looking to publish or share the framework.
 *
 * Here's a step-by-step breakdown of the task's behavior and functionality:
 *
 * 1. **Dependency Declaration**:
 *      - The task ensures that before zipping, the XCFramework corresponding to
 *        the given native build type has been fully assembled. This is guaranteed
 *        by setting a dependency on the specific XCFramework assemble task.
 *
 * 2. **Execution Configuration**:
 *      - The task is configured to take the built XCFramework (determined by
 *        the `getXCFrameworkPath` function) and compress it into a ZIP file.
 *      - The name of the ZIP file is derived from the project's name, version,
 *        and the specific native build type.
 *      - The final ZIP file is stored in the designated `swiftPackageBuildDirectory`.
 *
 * In conclusion, the `registerZipXCFrameworkTask` offers a streamlined way to
 * obtain a compressed version of the XCFramework, facilitating its distribution
 * or any subsequent tasks that require the zipped format.
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
 * Apple's XCFrameworks are a mechanism for bundling libraries and frameworks for various
 * platforms into a single package. When developers want to test the integration of the
 * package within their projects, having it in the right directory simplifies this
 * verification process. This task handles the movement of this bundled framework to the
 * desired local testing location.
 *
 * Here's a detailed overview of the task's behavior and functionalities:
 *
 * 1. **Local Testing Focus**:
 *      - This task is tailored for developers' local testing needs. By placing the
 *        XCFramework in a specific directory that mimics the structure of an actual
 *        Swift Package, developers can more easily verify its integrity, functionality,
 *        and integration.
 *
 * 2. **Dependency Declaration**:
 *      - Before initiating the copy operation, this task ensures that the XCFramework,
 *        identified by the given native build type, is fully assembled. This is achieved
 *        by establishing a dependency on the respective assemble task.
 *
 * 3. **Execution Configuration**:
 *      - The task is configured to move the XCFramework from its build location,
 *        denoted by `swiftPackageBuildDirectory`, to a Swift Package project directory,
 *        `swiftPackageProjectDirectory`.
 *      - This ensures that the XCFramework is easily accessible and recognizable for local
 *        testing and verifications.
 *
 * In a nutshell, the `registerCopyXCFrameworkTask` equips developers with a streamlined
 * approach to swiftly and accurately test the XCFramework's integration in a local setting.
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
     *
     * In Gradle, tasks can be categorized into groups for better organization and readability
     * when developers list or inspect tasks using tools or command-line interfaces. By assigning
     * tasks to specific groups, developers can more easily identify and execute related tasks
     * that serve a common purpose.
     *
     * The `SWIFT_PACKAGE_TASK_GROUP` specifically targets tasks related to operations around
     * Swift Packages.
     *
     * When tasks are associated with this group:
     *
     * - In the command-line interface (CLI), when a developer lists available tasks using
     *   Gradle commands, tasks under this group will be collectively displayed under the
     *   "Swift Package" header.
     * - In integrated development environments (IDEs) that support Gradle, these tasks might
     *   be collectively shown in a designated section, making it easier for developers to
     *   identify and run them.
     *
     * In essence, the `SWIFT_PACKAGE_TASK_GROUP` serves as a mechanism to improve developer
     * experience by clustering related tasks, thus providing clarity and ease of access.
     */
    const val SWIFT_PACKAGE_TASK_GROUP = "Swift Package"
}
