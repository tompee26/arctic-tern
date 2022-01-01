package com.tompee.arctictern.compiler

import com.squareup.kotlinpoet.ClassName
import com.tompee.arctictern.compiler.entities.Field

internal object Constants {

    /**
     * Shared Preferences field
     */
    val sharedPreferencesField = Field(
        "sharedPreferences",
        ClassName("android.content", "SharedPreferences")
    )
}
