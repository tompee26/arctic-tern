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

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
