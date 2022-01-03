package com.tompee.arctictern.compiler.extensions

import com.squareup.kotlinpoet.TypeName
import com.tompee.arctictern.compiler.supportedTypeMap

/**
 * Resolves the getter from a given type
 */
internal val TypeName.preferenceGetter: String
    get() = supportedTypeMap[this]?.first ?: throw IllegalArgumentException("Getter not found")

/**
 * Resolves the setter from a given type
 */
internal val TypeName.preferenceSetter: String
    get() = supportedTypeMap[this]?.second ?: throw IllegalArgumentException("Setter not found")
