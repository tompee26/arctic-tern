plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.diffplug.spotless")
    id("com.google.devtools.ksp")
}

apply("../spotless.gradle")

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        applicationId = "com.tompee.arctictern"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

val copyTaskProvider = tasks.register<Copy>("installGitHooks") {
    from("../scripts/hooks/pre-commit")
    into("../.git/hooks")
    fileMode = Integer.parseInt("0775", 8)
}

kotlin {
    sourceSets.debug {
        kotlin.srcDir("build/generated/ksp/debug/kotlin")
    }
    sourceSets.release {
        kotlin.srcDir("build/generated/ksp/release/kotlin")
    }
}

tasks.getByPath(":app:preBuild").dependsOn(copyTaskProvider.get())

dependencies {
    implementation(project(":nest"))
    compileOnly(project(":annotation"))
    ksp(project(":compiler"))

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.coroutines)

    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.appcompat)
}
