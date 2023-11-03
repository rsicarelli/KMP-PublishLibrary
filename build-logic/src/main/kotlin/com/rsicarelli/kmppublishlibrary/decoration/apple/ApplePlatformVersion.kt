package com.rsicarelli.kmppublishlibrary.decoration.apple

/**
 * Represents the version of an Apple platform or technology.
 */
sealed interface ApplePlatformVersion {

    val versionNumber: String

    data class IOS(override val versionNumber: String) : ApplePlatformVersion

    data class MacOS(override val versionNumber: String) : ApplePlatformVersion

    data class WatchOS(override val versionNumber: String) : ApplePlatformVersion

    data class TvOS(override val versionNumber: String) : ApplePlatformVersion

    data class Swift(override val versionNumber: String) : ApplePlatformVersion
}
