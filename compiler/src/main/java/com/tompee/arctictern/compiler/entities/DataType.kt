package com.tompee.arctictern.compiler.entities

import com.squareup.kotlinpoet.ClassName

internal data class DataType(
    val name: ClassName,
    val nullable: Boolean,
    val getter: String,
    val setter: String
)
