package com.rsicarelli.kmppublishlibrary.options

import com.rsicarelli.kmppublishlibrary.decoration.apple.ApplePlatformSettings
import com.rsicarelli.kmppublishlibrary.decoration.apple.AppleVersion

/**
 * Represents options related to a Swift package.
 *
 * @property swiftPackageDistributionURL URL for the Swift package distribution. By default,
 * it attempts to fetch the URL from the environment variable 'SWIFT_PACKAGE_DISTRIBUTION_URL'. If not found,
 * it defaults to 'distribution_url_not_set'.
 * @property appleTargetOptions Options detailing the Apple targets for the Swift package, which includes
 * Konan targets and their associated Apple versions.
 * @property swiftVersion Version of Swift used for the package.
 *
 * @see ApplePlatformSettings
 * @see AppleVersion
 */
data class SwiftPackageOptions(
    val swiftPackageDistributionURL: String = System.getenv(SwiftPackageOptionsDefaults.ENV_SWIFT_PACKAGE_DISTRIBUTION_URL)
        ?: "distribution_url_not_set",
    val appleTargetOptions: ApplePlatformSettings,
    val swiftVersion: AppleVersion.Swift,
)

private object SwiftPackageOptionsDefaults {

    const val ENV_SWIFT_PACKAGE_DISTRIBUTION_URL = "SWIFT_PACKAGE_DISTRIBUTION_URL"
}
