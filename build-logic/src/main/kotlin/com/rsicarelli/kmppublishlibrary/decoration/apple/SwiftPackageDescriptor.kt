package com.rsicarelli.kmppublishlibrary.decoration.apple

import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageDescriptor.LocalSwiftPackageDescriptor
import com.rsicarelli.kmppublishlibrary.decoration.apple.SwiftPackageDescriptor.RemoteSwiftPackageDescriptor
import groovy.lang.Writable
import groovy.text.SimpleTemplateEngine
import java.io.Writer
import java.net.URL

/**
 * Represents a configuration of a Swift package, which can be used to generate a
 * `Package.swift` file for Swift Package Manager. This is a sealed class which means
 * it has two possible concrete implementations: [LocalSwiftPackageDescriptor] for
 * configurations representing local Swift packages and [RemoteSwiftPackageDescriptor] for
 * configurations representing remote Swift packages.
 *
 * @param packageName The name of the Swift package.
 * @param zipFileName The name of the ZIP file containing the Swift package content.
 * @param swiftVersion The version of Swift tools used in the package.
 * @param platforms The platforms the package supports, e.g., `.iOS(.v15)`.
 * @param isLocal A boolean indicating whether the package is local or remote.
 */
internal sealed class SwiftPackageDescriptor(
    open val packageName: String,
    open val zipFileName: String,
    open val swiftVersion: AppleVersion.Swift,
    open val platforms: List<String>,
    private val isLocal: Boolean,
) {

    /**
     * Abstract function to fill properties required for generating `Package.swift`.
     */
    abstract fun fillProperties(map: HashMap<String, Any>)

    /**
     * Combines the common properties and the additional properties defined by
     * concrete implementations ([LocalSwiftPackageDescriptor] or [RemoteSwiftPackageDescriptor]).
     */
    private val templateProperties: HashMap<String, Any>
        get() {
            val map: HashMap<String, Any> = hashMapOf(
                "toolsVersion" to swiftVersion.versionNumber,
                "name" to packageName,
                "zipName" to zipFileName,
                "platforms" to platforms.joinToString { it },
                "isLocal" to isLocal,
            )
            fillProperties(map)
            return map
        }

    /**
     * Generates the `Package.swift` content based on the properties and writes
     * it to the provided writer.
     */
    fun writeTo(writer: Writer) {
        val writable: Writable = SimpleTemplateEngine()
            .createTemplate(swiftPackageTemplateURL)
            .make(templateProperties)
            ?: error("Failed to prepare Swift Package template: writable is null")

        writable.writeTo(writer)
    }

    /**
     * Represents a configuration for a local Swift package.
     *
     * @param packageName The name of the local Swift package.
     * @param zipFileName The name of the ZIP file containing the local Swift package content.
     */
    internal data class LocalSwiftPackageDescriptor(
        override val packageName: String,
        override val zipFileName: String,
        override val platforms: List<String>,
        override val swiftVersion: AppleVersion.Swift,
    ) : SwiftPackageDescriptor(
        packageName = packageName,
        zipFileName = zipFileName,
        isLocal = true,
        swiftVersion = swiftVersion,
        platforms = platforms
    ) {

        /**
         * For local packages, there are no additional properties required.
         */
        override fun fillProperties(map: HashMap<String, Any>) = Unit

        class Builder {

            lateinit var packageName: String
            lateinit var zipFileName: String
            lateinit var platforms: List<String>
            lateinit var swiftVersion: AppleVersion.Swift

            fun build() = LocalSwiftPackageDescriptor(
                packageName = packageName,
                zipFileName = zipFileName,
                platforms = platforms,
                swiftVersion = swiftVersion
            )
        }

        internal companion object {

            /**
             * Constructs and returns a [LocalSwiftPackageDescriptor] using a builder.
             */
            inline fun localSwiftPackageDescriptor(init: Builder.() -> Unit): LocalSwiftPackageDescriptor =
                Builder().apply(init).build()
        }
    }

    /**
     * Represents a configuration for a remote Swift package.
     *
     * @param packageName The name of the remote Swift package.
     * @param zipFileName The name of the ZIP file containing the remote Swift package content.
     * @param zipChecksum The checksum for the ZIP file.
     * @param distributionUrl The URL where the ZIP file is hosted.
     */
    internal data class RemoteSwiftPackageDescriptor(
        override val packageName: String,
        override val zipFileName: String,
        override val platforms: List<String>,
        override val swiftVersion: AppleVersion.Swift,
        var zipChecksum: String,
        var distributionUrl: String,
    ) : SwiftPackageDescriptor(
        packageName = packageName,
        zipFileName = zipFileName,
        isLocal = false,
        swiftVersion = swiftVersion,
        platforms = platforms
    ) {

        /**
         * Adds the checksum and the URL of the ZIP file to the properties.
         */
        override fun fillProperties(map: HashMap<String, Any>) {
            map["checksum"] = zipChecksum
            map["url"] = "$distributionUrl/$zipFileName"
        }

        class Builder {

            lateinit var packageName: String
            lateinit var zipFileName: String
            lateinit var zipChecksum: String
            lateinit var distributionUrl: String
            lateinit var platforms: List<String>
            lateinit var swiftVersion: AppleVersion.Swift

            fun build() = RemoteSwiftPackageDescriptor(
                packageName = packageName,
                zipFileName = zipFileName,
                zipChecksum = zipChecksum,
                distributionUrl = distributionUrl,
                platforms = platforms,
                swiftVersion = swiftVersion
            )
        }

        companion object {

            /**
             * Constructs and returns a [RemoteSwiftPackageDescriptor] using a builder.
             */
            internal inline fun remoteSwiftPackageDescriptor(init: Builder.() -> Unit): RemoteSwiftPackageDescriptor =
                Builder().apply(init).build()
        }
    }

    internal companion object {

        /**
         * Represents the default file name for a Swift Package descriptor.
         */
        const val SWIFT_PACKAGE_FILE_NAME = "Package.swift"

        /**
         * Lazy-loaded URL pointing to the template file location for the Swift Package descriptor.
         * This template is used to generate the `Package.swift` file based on specified configurations.
         */
        val swiftPackageTemplateURL: URL? by lazy {
            SwiftPackageDescriptor::class.java.getResource("/templates/$SWIFT_PACKAGE_FILE_NAME.template")
        }
    }
}


