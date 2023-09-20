package com.tompee.arctictern.compiler.entities

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

internal class Field(val name: String, val type: ClassName) {

    fun toParameterSpec(vararg modifiers: KModifier = emptyArray()): ParameterSpec {
        return toParameterSpecBuilder(*modifiers).build()
    }

    fun toParameterSpecBuilder(vararg modifiers: KModifier = emptyArray()): ParameterSpec.Builder {
        return ParameterSpec.builder(name, type, *modifiers)
    }

    fun toPropertySpec(
        withInitializer: Boolean = false,
        vararg modifiers: KModifier = emptyArray(),
    ): PropertySpec {
        return toPropertySpecBuilder(withInitializer, *modifiers).build()
    }

    fun toPropertySpecBuilder(
        withInitializer: Boolean = false,
        vararg modifiers: KModifier = emptyArray(),
    ): PropertySpec.Builder {
        return PropertySpec.builder(name, type, *modifiers)
            .apply { if (withInitializer) initializer(name) }
    }
}
