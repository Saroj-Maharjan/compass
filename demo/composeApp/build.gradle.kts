import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishAllLibraryVariants()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(projects.compassCore)
            implementation(projects.compassGeocoder)
            implementation(projects.compassGeocoderWebMapbox)
            implementation(projects.compassGeocoderWebGooglemaps)
            implementation(projects.compassGeocoderWebOpencage)
            implementation(projects.compassGeolocation)
            implementation(projects.compassAutocomplete)

            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(libs.bundles.voyager)
            implementation(libs.bundles.stateHolder)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val mobileMain by creating {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)
            dependencies {
                implementation(projects.compassGeocoderMobile)
                implementation(projects.compassGeolocationMobile)
                implementation(projects.compassPermissionsMobile)
                implementation(projects.compassAutocompleteMobile)
            }
        }


        val wasmJsMain by getting {
            dependencies {
                implementation(projects.compassGeolocationBrowser)
            }
        }

        val nonMobileMain by creating {
            dependsOn(commonMain.get())
            desktopMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies {
                implementation(projects.compassGeocoderWebGooglemaps)
                implementation(projects.compassAutocompleteGeocoderGooglemaps)
            }
        }

        val nonBrowser by creating {
            dependsOn(commonMain.get())
            desktopMain.dependsOn(this)
            androidMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "dev.jordond.compass.demo"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "dev.jordond.compass.demo"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.jordond.compass.demo"
            packageVersion = "1.0.0"
        }
    }
}