package com.tompee.arctictern.compiler.extensions

import com.squareup.kotlinpoet.TypeName
import com.tompee.arctictern.compiler.supportedTypes

/**
 * Resolves the getter from a given type
 */
internal fun TypeName.getPreferenceGetter(isNullable: Boolean): String {
    return supportedTypes.firstOrNull { it.name == this && it.nullable == isNullable }
        ?.getter
        ?: throw IllegalArgumentException("Getter not found")
}

/**
 * Resolves the setter from a given type
 */
internal fun TypeName.getPreferenceSetter(isNullable: Boolean): String {
    return supportedTypes.firstOrNull { it.name == this && it.nullable == isNullable }
        ?.setter
        ?: throw IllegalArgumentException("Getter not found")
}

/**
 * Converts a property declaration's type into a [Class]
 */
internal val TypeName.isSupportedType: Boolean
    get() = supportedTypes.any { it.name == this }

/**
 * Converts a property declaration's type into a [Class]
 */
internal fun TypeName.toNullable(isMutable: Boolean): TypeName {
    return if (isMutable) copy(true) else this
}
