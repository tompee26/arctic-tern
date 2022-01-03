package com.tompee.arctictern.compiler.extensions

import java.util.Locale

/**
 * Capitalizes the first character of the string
 */
internal fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
