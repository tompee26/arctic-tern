package com.tompee.arctictern.compiler.generators

import com.squareup.kotlinpoet.TypeSpec

/**
 * Parses the list of properties that will be the source of generated properties and functions
 */
internal interface MemberGenerator {

    /**
     * Applies all properties and functions
     */
    fun applyAll(builder: TypeSpec.Builder): TypeSpec.Builder
}
