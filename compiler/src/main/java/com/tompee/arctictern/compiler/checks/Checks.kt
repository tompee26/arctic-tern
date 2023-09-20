package com.tompee.arctictern.compiler.checks

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.tompee.arctictern.compiler.ProcessingException

/**
 * Assertion helper for [KSPropertyDeclaration]s
 */
internal fun KSPropertyDeclaration.assert(
    message: String,
    block: KSPropertyDeclaration.() -> Boolean,
) {
    if (!block(this)) {
        throw ProcessingException(message, this)
    }
}

/**
 * Assertion helper for [KSClassDeclaration]s
 */
internal fun KSClassDeclaration.assert(
    message: String,
    block: KSClassDeclaration.() -> Boolean,
) {
    if (!block(this)) {
        throw ProcessingException(message, this)
    }
}
