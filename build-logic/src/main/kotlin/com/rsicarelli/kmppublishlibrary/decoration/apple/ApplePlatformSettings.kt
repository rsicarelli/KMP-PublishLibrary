package com.rsicarelli.kmppublishlibrary.decoration.apple

import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.KonanTarget.IOS_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.IOS_SIMULATOR_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.IOS_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.MACOS_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.MACOS_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.TVOS_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.TVOS_SIMULATOR_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.TVOS_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.WATCHOS_ARM32
import org.jetbrains.kotlin.konan.target.KonanTarget.WATCHOS_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.WATCHOS_DEVICE_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.WATCHOS_SIMULATOR_ARM64
import org.jetbrains.kotlin.konan.target.KonanTarget.WATCHOS_X64

/**
 * Internal typealias to represent an Apple target.
 *
 * This typealias is used to define Apple targets in a Kotlin/Native project. An Apple target is
 * represented by a [Pair] of a [List] of [KonanTarget] and an [AppleVersion].
 *
 * @see KonanTarget
 * @see AppleVersion
 */
internal typealias AppleTarget = Pair<List<KonanTarget>, AppleVersion>

/**
 * Represents options for Apple targets, detailing the Konan targets and their associated Apple versions.
 *
 * @property iOSTargets Pair of a list of Konan targets for iOS and their corresponding Apple version.
 * @property watchOSTargets Pair of a list of Konan targets for watchOS and their corresponding Apple version.
 * @property macOSTargets Pair of a list of Konan targets for macOS and their corresponding Apple version.
 * @property tvOSTargets Pair of a list of Konan targets for tvOS and their corresponding Apple version.
 *
 * @see [KonanTarget]
 * @see [AppleVersion]
 */
data class ApplePlatformSettings(
    val iOSTargets: AppleTarget,
    val watchOSTargets: AppleTarget,
    val macOSTargets: AppleTarget,
    val tvOSTargets: AppleTarget,
) {

    init {
        iOSTargets.konanTargets.validateTargetFamily(Family.IOS)
        watchOSTargets.konanTargets.validateTargetFamily(Family.WATCHOS)
        macOSTargets.konanTargets.validateTargetFamily(Family.OSX)
        tvOSTargets.konanTargets.validateTargetFamily(Family.TVOS)

        require(
            value = iOSTargets.konanTargets.isNotEmpty() || watchOSTargets.konanTargets.isNotEmpty() ||
                macOSTargets.konanTargets.isNotEmpty() || tvOSTargets.konanTargets.isNotEmpty()
        ) {
            "At least one of the target lists (iOSTargets, watchOSTargets, macOSTargets, tvOSTargets) should have an item."
        }
    }

    /**
     * Converts the Apple targets into Kotlin native targets using the provided preset functions.
     *
     * @param presetFunctions Functions for converting Konan targets to Kotlin native targets.
     * @return List of converted Kotlin native targets.
     */
    internal fun toKotlinNativeTargets(presetFunctions: KotlinTargetContainerWithPresetFunctions): List<KotlinNativeTarget> =
        listOfNotNull(
            iOSTargets.konanTargets.mapKonanTargetsToKotlinNativeTargets(presetFunctions),
            watchOSTargets.konanTargets.mapKonanTargetsToKotlinNativeTargets(presetFunctions),
            macOSTargets.konanTargets.mapKonanTargetsToKotlinNativeTargets(presetFunctions),
            tvOSTargets.konanTargets.mapKonanTargetsToKotlinNativeTargets(presetFunctions)
        ).flatten()

    /**
     * Retrieves platform versions for all Apple targets defined in the options.
     */
    internal fun getAsPlatformVersions(): MutableList<String> =
        mutableListOf<String>().apply {
            iOSTargets.toPlatformString("iOS")?.let(::add)
            watchOSTargets.toPlatformString("watchOS")?.let(::add)
            macOSTargets.toPlatformString("macOS")?.let(::add)
            tvOSTargets.toPlatformString("tvOS")?.let(::add)
        }

    /**
     * Extension function to convert a pair of Konan targets and Apple version into a platform string representation.
     *
     * @param platform Name of the Apple platform (e.g., "iOS", "watchOS").
     * @return Platform string representation, or null if the list of Konan targets is empty.
     */
    private fun AppleTarget.toPlatformString(platform: String): String? =
        takeIf { it.konanTargets.isNotEmpty() }
            ?.let { ".$platform(.v${it.version})" }

    /**
     * Validates that all Konan targets in the list belong to the expected family.
     *
     * @param expectedFamily Expected family for all targets in the list.
     * @throws [IllegalArgumentException] if any invalid targets are found.
     */
    @Throws(IllegalArgumentException::class)
    private fun List<KonanTarget>.validateTargetFamily(expectedFamily: Family) {
        if (isEmpty()) return  // Allow empty targets

        val invalidTargets: List<KonanTarget> = filterNot { it.family == expectedFamily }
        require(invalidTargets.isEmpty()) {
            "$this contains invalid Konan targets: ${invalidTargets.joinToString(", ") { it.name }}. " +
                "Expected family: $expectedFamily."
        }
    }

    /**
     * Converts a list of [KonanTarget] to a list of [KotlinNativeTarget].
     *
     * @param container - The container holding the Kotlin target container with preset functions.
     * @return A list of [KotlinNativeTarget] converted from the input list of [KonanTarget].
     * @throws IllegalArgumentException if the provided [KonanTarget] is not supported.
     */
    private fun List<KonanTarget>.mapKonanTargetsToKotlinNativeTargets(
        container: KotlinTargetContainerWithPresetFunctions,
    ): List<KotlinNativeTarget> =
        map { target ->
            targetFunctionMap[target]?.invoke(container)
                ?: error("Target $target is not supported.")
        }

    /**
     * Getter for the `konanTargets` property of the [AppleTarget] class.
     *
     * This property represents the available Konan targets for the [AppleTarget].
     *
     * @return The first Konan target in the `konanTargets` list for the [AppleTarget].
     */
    private val AppleTarget.konanTargets
        get() = first

    /**
     * Returns the version number of the [AppleTarget].
     *
     * @return The version number.
     */
    private val AppleTarget.version
        get() = second

    companion object {

        /**
         * Mapping of Konan targets to their respective functions for conversion to Kotlin native targets.
         */
        private val targetFunctionMap: HashMap<KonanTarget, (KotlinTargetContainerWithPresetFunctions) -> KotlinNativeTarget> =
            hashMapOf(
                IOS_ARM64 to KotlinTargetContainerWithPresetFunctions::iosArm64,
                IOS_X64 to KotlinTargetContainerWithPresetFunctions::iosX64,
                IOS_SIMULATOR_ARM64 to KotlinTargetContainerWithPresetFunctions::iosSimulatorArm64,
                MACOS_ARM64 to KotlinTargetContainerWithPresetFunctions::macosArm64,
                IOS_ARM64 to KotlinTargetContainerWithPresetFunctions::iosArm64,
                IOS_X64 to KotlinTargetContainerWithPresetFunctions::iosX64,
                IOS_SIMULATOR_ARM64 to KotlinTargetContainerWithPresetFunctions::iosSimulatorArm64,
                MACOS_ARM64 to KotlinTargetContainerWithPresetFunctions::macosArm64,
                MACOS_X64 to KotlinTargetContainerWithPresetFunctions::macosX64,
                TVOS_ARM64 to KotlinTargetContainerWithPresetFunctions::tvosArm64,
                TVOS_SIMULATOR_ARM64 to KotlinTargetContainerWithPresetFunctions::tvosSimulatorArm64,
                TVOS_X64 to KotlinTargetContainerWithPresetFunctions::tvosX64,
                WATCHOS_ARM32 to KotlinTargetContainerWithPresetFunctions::watchosArm32,
                WATCHOS_ARM64 to KotlinTargetContainerWithPresetFunctions::watchosArm64,
                WATCHOS_DEVICE_ARM64 to KotlinTargetContainerWithPresetFunctions::watchosDeviceArm64,
                WATCHOS_SIMULATOR_ARM64 to KotlinTargetContainerWithPresetFunctions::watchosSimulatorArm64,
                WATCHOS_X64 to KotlinTargetContainerWithPresetFunctions::watchosX64,
            )

        /**
         * Default Apple target options with predefined Konan targets and Apple versions.
         */
        val default: ApplePlatformSettings
            get() = ApplePlatformSettings(
                iOSTargets = listOf(IOS_X64, IOS_ARM64, IOS_SIMULATOR_ARM64) to AppleVersion.IOS("17"),
                watchOSTargets = listOf(WATCHOS_ARM32, WATCHOS_ARM64, WATCHOS_SIMULATOR_ARM64) to AppleVersion.WatchOS("10"),
                macOSTargets = listOf(MACOS_ARM64, MACOS_X64) to AppleVersion.MacOS("14"),
                tvOSTargets = listOf(TVOS_X64, TVOS_ARM64, TVOS_SIMULATOR_ARM64) to AppleVersion.TvOS("17")
            )
    }
}
