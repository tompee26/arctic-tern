package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Converts a property declaration's type into a [Class]
 */
internal fun KSPropertyDeclaration.className(): ClassName {
    return type.resolve().toClassName()
}
