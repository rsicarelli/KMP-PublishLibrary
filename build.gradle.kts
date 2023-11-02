plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id(libs.plugins.rsicarelli.kmpPlublishLibrary.get().pluginId)
}
