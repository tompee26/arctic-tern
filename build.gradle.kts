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
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
