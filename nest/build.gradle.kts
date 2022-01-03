plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.diffplug.spotless")
    id("org.jetbrains.dokka") version Versions.dokka
    id("maven-publish")
}

apply("../spotless.gradle")

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

tasks.dokkaJavadoc.configure {
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(11)
            noAndroidSdkLink.set(false)
        }
    }
}

ext {
    set("PUBLISH_ARTIFACT_ID", "arctic-tern-nest")
    set("PUBLISH_DESCRIPTION", "Android package for Arctic Tern")
}

apply("../scripts/publish-module.gradle")

dependencies {
    implementation(Libs.Kotlin.stdlib)
    api(Libs.Kotlin.coroutines)
    implementation(Libs.AndroidX.core)
}