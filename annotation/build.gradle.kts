plugins {
    id("java-library")
    id("kotlin")
    id("com.diffplug.spotless")
    id("org.jetbrains.dokka") version Versions.dokka
    id("maven-publish")
}

apply("../spotless.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    set("PUBLISH_ARTIFACT_ID", "arctic-tern-annotation")
    set("PUBLISH_DESCRIPTION", "Annotation package for Arctic Tern")
}

apply("../scripts/publish-module.gradle")

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.coroutines)
}
