package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Converts a property declaration's type into a [Class]
 */
internal val KSPropertyDeclaration.typeName: TypeName
    get() = type.resolve().toTypeName()

/**
 * Returns true if type is explicitly marked as nullable
 */
internal val KSPropertyDeclaration.isNullable: Boolean
    get() = type.resolve().isMarkedNullable
