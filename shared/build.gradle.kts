plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // Android library target (AGP 9's built-in KMP plugin). Consumed by :app as
    // a regular library dependency. Uses the new androidLibrary { } DSL rather
    // than a separate android { } block.
    androidLibrary {
        namespace = "com.mobile.tamatami.shared"
        compileSdk = 36
        minSdk = 31
    }

    // JVM target lets us compile + run the shared code (and its tests) here,
    // without Xcode. This is what validates the shared core in this environment.
    jvm()

    // iOS targets. Building/linking a runnable framework requires Xcode's Apple
    // SDKs, but Kotlin/Native compiles the shared code for these targets here
    // (konan toolchain) — so the shared core is validated for iOS too.
    //
    // Each target produces a `Shared.framework` the SwiftUI app links against.
    // Dynamic (not static): this is what embedAndSignAppleFrameworkForXcode
    // embeds into the app bundle, and it lets the linker resolve the ObjC
    // classes via `-framework Shared` (a static framework needs force-loading).
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        // JVM (host) tests + any JVM consumer use the JDBC/sqlite driver.
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

sqldelight {
    databases {
        create("TamatamiDb") {
            packageName.set("com.mobile.tamatami.db")
        }
    }
}
