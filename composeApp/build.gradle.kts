import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
//    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
//        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.ruko.system")
            isStatic = true
        }
    }

    // Native Firebase frameworks are linked through Swift Package Manager in iosApp.
    sourceSets {
        androidMain.dependencies {

            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.work.manager)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.15.0"))
            implementation("com.google.firebase:firebase-appcheck-playintegrity")
            implementation("com.google.firebase:firebase-appcheck-debug")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.qr.kit)
            implementation(libs.backhandler)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.moko.permission)
            implementation(libs.moko.permission.compose)
            implementation(libs.moko.permission.notification)

//Alarm - Notification
//            implementation(libs.alarmee)
            implementation(libs.koin.core)
            implementation(libs.koin.compose) //koin-core
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.navigation)
            implementation(libs.gitlive.firebase.analytics)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.functions)
            implementation(libs.firebase.messaging)
//            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.firestore)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.ruko.system"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ruko.system"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
dependencies {
    debugImplementation(compose.uiTooling)
}
ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}
