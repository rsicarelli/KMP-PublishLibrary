import com.rsicarelli.kmppublishlibrary.multiplatformLibrary
import com.rsicarelli.kmppublishlibrary.multiplatformLibraryPublication

plugins {
    kotlin("multiplatform")
    id(libs.plugins.android.library.get().pluginId)
}

version = "0.1"

kotlin {
    jvmToolchain(17)
}

multiplatformLibrary()
multiplatformLibraryPublication()

kotlin {
    sourceSets {
        commonMain {
            dependencies { }
        }

        commonTest {
            dependencies { }
        }

        androidMain {
            dependencies { }
        }

        jvmMain {
            dependencies { }
        }

        iosMain {
            dependencies { }
        }

        watchosMain {
            dependencies { }
        }

        macosMain {
            dependencies { }
        }

        jsMain {
            dependencies { }
        }

        tvosMain {
            dependencies { }
        }
    }
}
