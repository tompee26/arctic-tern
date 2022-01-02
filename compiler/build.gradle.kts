plugins {
    id("java-library")
    id("kotlin")
    id("com.diffplug.spotless")
    id("com.google.devtools.ksp")
}

apply("../spotless.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
    kotlinOptions.freeCompilerArgs += "-opt-in=com.google.devtools.ksp.KspExperimental"
}

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
