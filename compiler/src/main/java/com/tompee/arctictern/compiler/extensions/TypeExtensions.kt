package com.tompee.arctictern.compiler.extensions

import com.squareup.kotlinpoet.TypeName
import com.tompee.arctictern.compiler.supportedTypeMap

/**
 * Resolves the getter from a given type
 */
internal fun TypeName.getPreferenceGetter(): String {
    return supportedTypeMap[this]?.first ?: throw IllegalArgumentException("Getter not found")
}

/**
 * Resolves the setter from a given type
 */
internal fun TypeName.getPreferenceSetter(): String {
    return supportedTypeMap[this]?.second ?: throw IllegalArgumentException("Setter not found")
}
