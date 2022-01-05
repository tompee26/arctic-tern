package com.tompee.arctictern.compiler

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.tompee.arctictern.compiler.entities.DataType
import com.tompee.arctictern.compiler.entities.Field

/**
 * Set of supported files
 */
internal val supportedTypes = setOf(
    DataType(INT, "getInt(%L, %L)", "putInt(%L, %L)"),
    DataType(BOOLEAN, "getBoolean(%L, %L)", "putBoolean(%L, %L)"),
    DataType(FLOAT, "getFloat(%L, %L)", "putFloat(%L, %L)"),
    DataType(LONG, "getLong(%L, %L)", "putLong(%L, %L)"),
    DataType(STRING.copy(true), "getString(%L, %L)", "putString(%L, %L)"),
    DataType(STRING, "getString(%L, %L).orEmpty()", "putString(%L, %L)"),
    DataType(
        SET.parameterizedBy(STRING).copy(true),
        "getStringSet(%L, %L)",
        "putStringSet(%L, %L)"
    ),
    DataType(
        SET.parameterizedBy(STRING),
        "getStringSet(%L, %L).orEmpty()",
        "putStringSet(%L, %L)"
    ),
    DataType(
        SET.parameterizedBy(STRING.copy(true)).copy(true),
        "getStringSet(%L, %L)",
        "putStringSet(%L, %L)"
    ),
    DataType(
        SET.parameterizedBy(STRING.copy(true)),
        "getStringSet(%L, %L).orEmpty()",
        "putStringSet(%L, %L)"
    ),
)

/**
 * Context field
 */
internal val contextField = Field(
    "context",
    ClassName("android.content", "Context")
)

/**
 * Shared Preferences field
 */
internal val sharedPreferencesField = Field(
    "sharedPreferences",
    ClassName("android.content", "SharedPreferences")
)

/**
 * Arctic Tern Preference field
 */
internal val preferenceField = Field(
    "preference",
    ClassName("com.tompee.arctictern.nest", "Preference")
)

/**
 * Flow field
 */
internal val flowField = Field(
    "flow",
    ClassName("kotlinx.coroutines.flow", "Flow")
)

/**
 * Migratable field
 */
internal val migratableField = Field(
    "migratable",
    ClassName("com.tompee.arctictern.nest", "Migratable")
)
