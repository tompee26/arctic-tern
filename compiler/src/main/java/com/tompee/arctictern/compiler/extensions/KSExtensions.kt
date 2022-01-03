package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Converts a property declaration's type into a [Class]
 */
internal val KSPropertyDeclaration.className: ClassName
    get() = type.resolve().toClassName()

/**
 * Returns true if type is explicitly marked as nullable
 */
internal val KSPropertyDeclaration.isNullable: Boolean
    get() = type.resolve().isMarkedNullable
