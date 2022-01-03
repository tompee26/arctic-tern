package com.tompee.arctictern.compiler

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.tompee.arctictern.compiler.entities.Field

/**
 * Supported native data types
 */
internal val supportedTypeMap = mapOf(
    INT to ("getInt" to "putInt"),
    BOOLEAN to ("getBoolean" to "putBoolean")
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
