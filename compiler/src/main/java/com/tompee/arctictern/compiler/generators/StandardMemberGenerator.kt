package com.tompee.arctictern.compiler.generators

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.tompee.arctictern.compiler.checks.assert
import com.tompee.arctictern.compiler.extensions.capitalize
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.isSupportedType
import com.tompee.arctictern.compiler.extensions.preferenceGetter
import com.tompee.arctictern.compiler.extensions.preferenceSetter
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.compiler.extensions.typeName
import com.tompee.arctictern.compiler.flowField
import com.tompee.arctictern.compiler.preferenceField
import com.tompee.arctictern.compiler.sharedPreferencesField
import com.tompee.arctictern.nest.ArcticTern

internal class StandardMemberGenerator(classDeclaration: KSClassDeclaration) :
    MemberGenerator<ArcticTern.Property> {

    /**
     * Property associated with [ArcticTern.Property]
     */
    private data class Property(
        val prop: KSPropertyDeclaration,
        val annotation: ArcticTern.Property
    )

    /**
     * Annotated properties
     */
    private val properties = classDeclaration.getAllProperties()
        .filter { it.getAnnotationsByType(ArcticTern.Property::class).any() }
        .onEach {
            it.assert("Only var properties are allowed") { isMutable }
            it.assert("Properties must be open") { isOpen() }
            it.assert("Unsupported type ${it.typeName}") { typeName.isSupportedType }
        }
        .map { Property(it, it.getAnnotationsByType(ArcticTern.Property::class).first()) }
        .toList()

    /**
     * Applies the lazy shared preferences
     */
    override fun applyAll(builder: TypeSpec.Builder): TypeSpec.Builder {
        return builder.addProperties(
            properties.map {
                val internalPropName = "${it.prop.simpleName.asString()}Internal"
                listOfNotNull(
                    buildLazyPreferenceProperty(internalPropName, it),
                    buildPropertyOverride(internalPropName, it),
                    if (it.annotation.withFlow)
                        buildFlowProperty(internalPropName, it)
                    else null
                )
            }.flatten()
        ).addFunctions(
            properties.mapNotNull {
                val internalPropName = "${it.prop.simpleName.asString()}Internal"
                if (it.annotation.withDelete) buildDeleteFunction(internalPropName, it) else null
            }
        )
    }

    /**
     * Builds the private lazy ArcticTernPreference property
     */
    private fun buildLazyPreferenceProperty(
        internalPropName: String,
        property: Property
    ): PropertySpec {
        val preferenceName = "preference"
        val keyName = "key"
        val defaultValueName = "defaultValue"
        val valueName = "value"
        return PropertySpec.builder(
            internalPropName,
            preferenceField.type
                .parameterizedBy(property.prop.let { it.typeName.toNullable(it.isNullable) }),
            KModifier.PRIVATE
        )
            .delegate(
                CodeBlock.builder()
                    .beginControlFlow("lazy")
                    .addStatement("%L(", preferenceField.type.simpleName)
                    .addStatement("key = %S,", property.annotation.getKey(property.prop))
                    .addStatement(
                        "defaultValue = super.%L,",
                        property.prop.simpleName.asString()
                    )
                    .beginControlFlow("valueProvider = ")
                    .add(
                        CodeBlock.builder()
                            .addStatement(
                                "%L, %L, %L ->",
                                preferenceName,
                                keyName,
                                defaultValueName
                            )
                            .addStatement(
                                "$preferenceName.${property.prop.typeName.preferenceGetter}",
                                keyName,
                                defaultValueName,
                            )
                            .build()
                    )
                    .endControlFlow()
                    .addStatement(",")
                    .beginControlFlow("valueSetter = ")
                    .add(
                        CodeBlock.Builder()
                            .addStatement(
                                "%L, %L, %L ->",
                                preferenceName,
                                keyName,
                                valueName
                            )
                            .addStatement(
                                "$preferenceName.edit().${property.prop.typeName.preferenceSetter}.apply()",
                                keyName,
                                valueName
                            )
                            .build()
                    )
                    .endControlFlow()
                    .addStatement(",")
                    .addStatement(
                        "%L = %L",
                        sharedPreferencesField.name,
                        sharedPreferencesField.name
                    )
                    .addStatement(")")
                    .endControlFlow()
                    .build()
            )
            .build()
    }

    /**
     * Builds the property override
     */
    private fun buildPropertyOverride(
        internalPropName: String,
        property: Property
    ): PropertySpec {
        return PropertySpec.builder(
            property.prop.simpleName.asString(),
            property.prop.let { it.typeName.toNullable(it.isNullable) },
            KModifier.OVERRIDE
        )
            .mutable(true)
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(
                        ParameterSpec.builder("value", property.prop.typeName)
                            .build()
                    )
                    .addStatement("%L.value = value", internalPropName)
                    .build()
            )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L.value", internalPropName)
                    .build()
            )
            .build()
    }

    /**
     * Builds the flow property
     */
    private fun buildFlowProperty(
        internalPropName: String,
        property: Property
    ): PropertySpec {
        return PropertySpec.builder(
            "${property.prop.simpleName.asString()}Flow",
            flowField.type
                .parameterizedBy(property.prop.let { it.typeName.toNullable(it.isNullable) })
        )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L.observe()", internalPropName)
                    .build()
            )
            .build()
    }

    /**
     * Builds the delete function
     */
    private fun buildDeleteFunction(
        internalPropName: String,
        property: Property
    ): FunSpec {
        return FunSpec.builder("delete${property.prop.simpleName.asString().capitalize()}")
            .addStatement("%L.delete()", internalPropName)
            .build()
    }
}
