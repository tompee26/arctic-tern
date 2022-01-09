package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.tompee.arctictern.nest.ArcticTern
import com.tompee.arctictern.nest.DEFAULT_KEY

/**
 * Returns the key that will be used as a shared preference key.
 * Will generate it from [property] if not provided
 */
internal fun ArcticTern.Property.getKey(property: KSPropertyDeclaration): String {
    return if (key == DEFAULT_KEY) {
        "key_${property.simpleName.asString()}"
    } else key
}

/**
 * Returns the key that will be used as a shared preference key.
 * Will generate it from [property] if not provided
 */
internal fun ArcticTern.ObjectProperty.getKey(property: KSPropertyDeclaration): String {
    return if (key == DEFAULT_KEY) {
        "key_${property.simpleName.asString()}"
    } else key
}

/**
 * Returns the key that will be used as a shared preference key.
 * Will generate it from [property] if not provided
 */
internal fun ArcticTern.NullableObjectProperty.getKey(property: KSPropertyDeclaration): String {
    return if (key == DEFAULT_KEY) {
        "key_${property.simpleName.asString()}"
    } else key
}
