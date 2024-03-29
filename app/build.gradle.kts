plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.diffplug.spotless")
    id("com.google.devtools.ksp")
}

apply("../spotless.gradle")

android {
    namespace = "com.tompee.arctictern"
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.tompee.arctictern.Runner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
    sourceSets.androidTestDebug {
        kotlin.srcDir("build/generated/ksp/debugAndroidTest/kotlin")
    }
}

tasks.getByPath(":app:preBuild").dependsOn(copyTaskProvider.get())

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.coroutines)

    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.appcompat)

    androidTestImplementation(project(":nest"))
    androidTestImplementation(project(":annotation"))
    kspAndroidTest(project(":compiler"))
    androidTestImplementation(Libs.AndroidX.Test.runner)
    androidTestImplementation(Libs.AndroidX.Test.rules)
    androidTestImplementation(Libs.AndroidX.Test.ext)
    androidTestImplementation(Libs.Kotlin.coroutinesTest)
}
