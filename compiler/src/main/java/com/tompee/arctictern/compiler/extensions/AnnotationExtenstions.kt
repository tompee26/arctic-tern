package com.tompee.arctictern.compiler.extensions

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.tompee.arctictern.nest.ArcticTern

/**
 * Returns the key that will be used as a shared preference key.
 * Will generate it from [property] if not provided
 */
internal fun ArcticTern.Property.getKey(property: KSPropertyDeclaration): String {
    return if (key == "arcticterndefault") {
        "key_${property.simpleName.asString()}"
    } else key
}
