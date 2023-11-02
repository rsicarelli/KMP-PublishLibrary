package com.rsicarelli.kmppublishlibrary.decoration.apple

/**
 * Represents the version of an Apple platform or technology.
 */
sealed interface AppleVersion {

    val versionNumber: String

    data class IOS(override val versionNumber: String) : AppleVersion

    data class MacOS(override val versionNumber: String) : AppleVersion

    data class WatchOS(override val versionNumber: String) : AppleVersion

    data class TvOS(override val versionNumber: String) : AppleVersion

    data class Swift(override val versionNumber: String) : AppleVersion
}
