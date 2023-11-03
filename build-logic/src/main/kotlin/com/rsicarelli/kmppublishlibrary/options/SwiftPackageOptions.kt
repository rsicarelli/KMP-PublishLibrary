package com.rsicarelli.kmppublishlibrary.options

import com.rsicarelli.kmppublishlibrary.decoration.apple.ApplePlatformSettings
import com.rsicarelli.kmppublishlibrary.decoration.apple.ApplePlatformVersion

/**
 * Represents options related to a Swift package.
 *
 * @see ApplePlatformSettings
 * @see ApplePlatformVersion
 */
data class SwiftPackageOptions(
    val swiftPackageDistributionURL: String = System.getenv(SwiftPackageOptionsDefaults.ENV_SWIFT_PACKAGE_DISTRIBUTION_URL)
        ?: "distribution_url_not_set",
    val appleTargetOptions: ApplePlatformSettings,
    val swiftVersion: ApplePlatformVersion.Swift,
)

private object SwiftPackageOptionsDefaults {

    const val ENV_SWIFT_PACKAGE_DISTRIBUTION_URL = "SWIFT_PACKAGE_DISTRIBUTION_URL"
}
