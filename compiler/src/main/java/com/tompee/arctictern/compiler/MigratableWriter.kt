package com.tompee.arctictern.compiler

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.tompee.arctictern.nest.ArcticTern

internal class MigratableWriter(private val annotation: ArcticTern) {

    companion object {

        private const val VERSION_CONST_NAME = "VERSION_CODE"
        private const val VERSION_KEY_NAME = "KEY_VERSION"
        private const val VERSION_KEY_VALUE = "\"arctic.pref.key.version\""
    }

    fun applyCompanion(companionBuilder: TypeSpec.Builder): TypeSpec.Builder {
        return companionBuilder.applyConstants()
    }

    fun apply(builder: TypeSpec.Builder): TypeSpec.Builder {
        return builder.applyInitialize()
            .applyIsUpdated()
            .applyMigrate()
    }

    private fun TypeSpec.Builder.applyConstants(): TypeSpec.Builder {
        return addProperty(
            PropertySpec.builder(VERSION_CONST_NAME, INT, KModifier.PRIVATE, KModifier.CONST)
                .initializer(annotation.version.toString())
                .build()
        ).addProperty(
            PropertySpec.builder(
                VERSION_KEY_NAME,
                STRING,
                KModifier.PRIVATE,
                KModifier.CONST
            )
                .initializer(VERSION_KEY_VALUE)
                .build()
        )
    }

    private fun TypeSpec.Builder.applyInitialize(): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("initialize")
                .addModifiers(KModifier.OVERRIDE)
                .beginControlFlow(
                    "if (!%L.contains(%L))",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME
                )
                .addStatement(
                    "%L.edit().putInt(%L, %L).apply()",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                    VERSION_CONST_NAME
                )
                .endControlFlow()
                .build()
        )
    }

    private fun TypeSpec.Builder.applyIsUpdated(): TypeSpec.Builder {
        return addProperty(
            PropertySpec.builder("isUpdated", BOOLEAN, KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "if (!%L.contains(%L)) return false",
                            sharedPreferencesField.name,
                            VERSION_KEY_NAME
                        )
                        .addStatement(
                            "return %L.getInt(%L, 0) == %L",
                            sharedPreferencesField.name, VERSION_KEY_NAME, VERSION_CONST_NAME
                        )
                        .build()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.applyMigrate(): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("migrate")
                .addModifiers(KModifier.OVERRIDE)
                .addComment("TODO: Migration logic")
                .addStatement(
                    "%L.edit().putInt(%L, %L).apply()",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME, VERSION_CONST_NAME
                )
                .build()
        )
    }
}
