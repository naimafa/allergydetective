import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    android {
       namespace = "com.nfar.allergydetective.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.activity.compose)
            // Encrypted database on Android (feedback 20.11.): SQLCipher + the SupportSQLite connection of androidx.sqlite.
            implementation(libs.sqlcipher.android)
            implementation(libs.sqlite.framework)
            // Reads the ingredient list from a photo of a package (user 27.11.2026); the Latin model is bundled, works offline.
            implementation(libs.mlkit.text.recognition)
            // Crop screen after the photo of a package (user 27.11.2026): only the ingredient list is read.
            implementation(libs.image.cropper)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.room3.runtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.navigation.compose)
            implementation(libs.compose.material.icons)
        }
        iosMain.dependencies {
            // iOS keeps the bundled SQLite until its SQLCipher driver follows; Android uses SQLCipher.
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

room3 {
    // Exported schema JSON files are committed to Git (needed for migrations)
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.room3.compiler)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.compiler)
}
// Workaround: AGP lint tasks read KSP-generated sources without declaring a task dependency.
// Remove once AGP/KSP wire this up themselves.
tasks.matching { it.name.startsWith("lintAnalyze") || it.name.endsWith("LintModel") }.configureEach {
    dependsOn(tasks.matching { it.name.startsWith("kspAndroid") })
}
