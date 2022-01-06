object Libs {

    const val androidPlugin = "com.android.tools.build:gradle:${Versions.agp}"

    object Kotlin {
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
        const val coroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:${Versions.AndroidX.core}"
        const val appcompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appcompat}"

        object Test {
            const val runner = "androidx.test:runner:${Versions.AndroidX.test}"
            const val rules = "androidx.test:rules:${Versions.AndroidX.test}"
            const val ext = "androidx.test.ext:junit:${Versions.AndroidX.testExt}"
        }
    }

    const val spotlessPlugin = "com.diffplug.spotless:spotless-plugin-gradle:${Versions.spotless}"

    object KSP {
        const val plugin =
            "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${Versions.ksp}"
        const val core = "com.google.devtools.ksp:symbol-processing:${Versions.ksp}"
        const val api = "com.google.devtools.ksp:symbol-processing-api:${Versions.ksp}"
    }

    object AutoService {
        const val core =
            "com.google.auto.service:auto-service-annotations:${Versions.AutoService.core}"
        const val ksp = "dev.zacsweers.autoservice:auto-service-ksp:${Versions.AutoService.ksp}"
    }

    object KotlinPoet {
        const val core = "com.squareup:kotlinpoet:${Versions.KotlinPoet.core}"
        const val metadata = "com.squareup:kotlinpoet-metadata:${Versions.KotlinPoet.core}"
        const val ksp = "com.squareup:kotlinpoet-ksp:${Versions.KotlinPoet.core}"
        const val metadatajvm =
            "org.jetbrains.kotlinx:kotlinx-metadata-jvm:${Versions.KotlinPoet.jvm}"
    }
}