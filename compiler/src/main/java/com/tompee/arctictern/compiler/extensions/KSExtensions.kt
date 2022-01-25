package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Converts a property declaration's type into a [Class]
 */
internal val KSPropertyDeclaration.typeName: TypeName
    get() = type.toTypeName()

/**
 * Returns true if type is explicitly marked as nullable
 */
internal val KSPropertyDeclaration.isNullable: Boolean
    get() = type.resolve().isMarkedNullable

/**
 * Returns the annotation from a property
 */
internal inline fun <reified T : Annotation> KSPropertyDeclaration.getAnnotation(): KSAnnotation {
    return annotations.first { it.shortName.asString() == T::class.simpleName.orEmpty() }
}
