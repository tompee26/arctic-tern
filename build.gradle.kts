// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Libs.androidPlugin)
        classpath(Libs.Kotlin.plugin)
        classpath(Libs.spotlessPlugin)
        classpath(Libs.KSP.plugin)
    }
}
project.ext {
    set("PUBLISH_GROUP_ID", "io.github.tompee26")
    set("PUBLISH_VERSION", "0.0.2")
    set("PUBLISH_URL", "https://github.com/tompee26/arctic-tern")
    set("PUBLISH_LICENSE_NAME", "MIT License")
    set("PUBLISH_LICENSE_URL", "https://github.com/tompee26/arctic-tern/blob/main/LICENSE.md")
    set("PUBLISH_DEVELOPER_ID", "tompee26")
    set("PUBLISH_DEVELOPER_NAME", "Tompee Balauag")
    set("PUBLISH_DEVELOPER_EMAIL", "tompee26@gmail.com")
    set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/tompee26/arctic-tern.git")
    set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/tompee26/arctic-tern.git")
    set("PUBLISH_SCM_URL", "https://github.com/tompee26/arctic-tern/tree/master")
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}
apply("scripts/publish-root.gradle")
