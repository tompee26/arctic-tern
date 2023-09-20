plugins {
    id("java-library")
    id("kotlin")
    id("com.diffplug.spotless")
    id("com.google.devtools.ksp")
    id("org.jetbrains.dokka") version Versions.dokka
    id("maven-publish")
}

apply("../spotless.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=com.google.devtools.ksp.KspExperimental",
            "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        )
    }
}

tasks.dokkaJavadoc.configure {
    dokkaSourceSets {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(11)
            }
        }
    }
}

ext {
    set("PUBLISH_ARTIFACT_ID", "arctic-tern-compiler")
    set("PUBLISH_DESCRIPTION", "Compiler package for Arctic Tern")
}

apply("../scripts/publish-module.gradle")

dependencies {
    implementation(project(":annotation"))
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.coroutines)

    implementation(Libs.KSP.core)
    implementation(Libs.KSP.api)

    implementation(Libs.AutoService.core)
    ksp(Libs.AutoService.ksp)

    implementation(Libs.KotlinPoet.core)
    implementation(Libs.KotlinPoet.metadata)
    implementation(Libs.KotlinPoet.ksp)
    implementation(Libs.KotlinPoet.metadatajvm)
}
