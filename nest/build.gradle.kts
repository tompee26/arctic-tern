plugins {
    id("java-library")
    id("kotlin")
    id("com.diffplug.spotless")
}

apply("../spotless.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.coroutines)
}
