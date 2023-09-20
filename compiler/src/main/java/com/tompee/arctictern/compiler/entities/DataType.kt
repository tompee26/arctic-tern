package com.tompee.arctictern.compiler.entities

import com.squareup.kotlinpoet.TypeName

internal data class DataType(
    val name: TypeName,
    val getter: String,
    val setter: String,
)
