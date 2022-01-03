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
    DataType(INT, true, "getInt", "putInt"),
    DataType(INT, false, "getInt", "putInt"),
    DataType(BOOLEAN, true, "getBoolean", "putBoolean"),
    DataType(BOOLEAN, false, "getBoolean", "putBoolean"),
    DataType(FLOAT, true, "getFloat", "putFloat"),
    DataType(FLOAT, false, "getFloat", "putFloat"),
    DataType(LONG, true, "getLong", "putLong"),
    DataType(LONG, false, "getLong", "putLong"),
    DataType(STRING, true, "getString", "putString"),
    DataType(STRING, false, "getString", "putString"),
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
