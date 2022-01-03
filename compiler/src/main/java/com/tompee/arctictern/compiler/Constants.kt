package com.tompee.arctictern.compiler

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.tompee.arctictern.compiler.entities.DataType
import com.tompee.arctictern.compiler.entities.Field

/**
 * Set of supported files
 * // TODO: Support collection types and custom types
 */
internal val supportedTypes = setOf(
    DataType(INT, true, "getInt(%L, %L)", "putInt(%L, %L)"),
    DataType(INT, false, "getInt(%L, %L)", "putInt(%L, %L)"),
    DataType(BOOLEAN, true, "getBoolean(%L, %L)", "putBoolean(%L, %L)"),
    DataType(BOOLEAN, false, "getBoolean(%L, %L)", "putBoolean(%L, %L)"),
    DataType(FLOAT, true, "getFloat(%L, %L)", "putFloat(%L, %L)"),
    DataType(FLOAT, false, "getFloat(%L, %L)", "putFloat(%L, %L)"),
    DataType(LONG, true, "getLong(%L, %L)", "putLong(%L, %L)"),
    DataType(LONG, false, "getLong(%L, %L)", "putLong(%L, %L)"),
    DataType(STRING, true, "getString(%L, %L)", "putString(%L, %L)"),
    DataType(STRING, false, "getString(%L, %L).orEmpty()", "putString(%L, %L)"),
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
    "arcticTernPreference",
    ClassName("com.tompee.arctictern.nest", "ArcticTernPreference")
)

/**
 * Flow field
 */
internal val flowField = Field(
    "flow",
    ClassName("kotlinx.coroutines.flow", "Flow")
)
