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
 * An Apple target is represented by a [Pair] of a [List] of [KonanTarget] and an [ApplePlatformVersion].
 */
internal typealias AppleTarget = Pair<List<KonanTarget>, ApplePlatformVersion>

private val AppleTarget.konanTargets
    get() = first

private val AppleTarget.version
    get() = second

/**
 * Represents options for Apple targets, detailing the [KonanTarget] targets and their associated [ApplePlatformVersion].
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

    @Throws(IllegalArgumentException::class)
    private fun List<KonanTarget>.validateTargetFamily(expectedFamily: Family) {
        if (isEmpty()) return  // Allow empty targets

        val invalidTargets: List<KonanTarget> = filterNot { it.family == expectedFamily }
        require(invalidTargets.isEmpty()) {
            """$this contains invalid Konan targets: ${
                invalidTargets.joinToString(
                    ", ",
                    transform = KonanTarget::name
                )
            }. Expected family: $expectedFamily."""
        }
    }

    /**
     * Converts the Apple targets into Kotlin native targets using the provided preset functions.
     *
     * @see KotlinTargetContainerWithPresetFunctions
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
    internal fun getAsPlatformVersions(): MutableList<String> {
        fun AppleTarget.toPlatformString(platform: String): String? =
            takeIf { it.konanTargets.isNotEmpty() }
                ?.let { ".$platform(.v${it.version})" }

        return mutableListOf<String>().apply {
            iOSTargets.toPlatformString("iOS")?.let(::add)
            watchOSTargets.toPlatformString("watchOS")?.let(::add)
            macOSTargets.toPlatformString("macOS")?.let(::add)
            tvOSTargets.toPlatformString("tvOS")?.let(::add)
        }
    }

    /**
     * Converts a list of [KonanTarget] to a list of [KotlinNativeTarget].
     */
    private fun List<KonanTarget>.mapKonanTargetsToKotlinNativeTargets(
        container: KotlinTargetContainerWithPresetFunctions,
    ): List<KotlinNativeTarget> =
        map { target ->
            targetFunctionMap[target]?.invoke(container)
                ?: error("Target $target is not supported.")
        }

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
                iOSTargets = listOf(IOS_X64, IOS_ARM64, IOS_SIMULATOR_ARM64) to ApplePlatformVersion.IOS("17"),
                watchOSTargets = listOf(WATCHOS_ARM32, WATCHOS_ARM64, WATCHOS_SIMULATOR_ARM64) to ApplePlatformVersion.WatchOS("10"),
                macOSTargets = listOf(MACOS_ARM64, MACOS_X64) to ApplePlatformVersion.MacOS("14"),
                tvOSTargets = listOf(TVOS_X64, TVOS_ARM64, TVOS_SIMULATOR_ARM64) to ApplePlatformVersion.TvOS("17")
            )
    }
}
