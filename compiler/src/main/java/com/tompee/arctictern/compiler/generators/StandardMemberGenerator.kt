package com.tompee.arctictern.compiler.generators

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.tompee.arctictern.compiler.checks.assert
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.isSupportedType
import com.tompee.arctictern.compiler.extensions.preferenceGetter
import com.tompee.arctictern.compiler.extensions.preferenceSetter
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.compiler.extensions.typeName
import com.tompee.arctictern.compiler.preferenceField
import com.tompee.arctictern.compiler.sharedPreferencesField
import com.tompee.arctictern.nest.ArcticTern

internal class StandardMemberGenerator(classDeclaration: KSClassDeclaration) :
    BaseMemberGenerator() {

    /**
     * Property associated with [ArcticTern.Property]
     */
    private data class Property(
        val name: String,
        val declaration: KSPropertyDeclaration,
        val annotation: ArcticTern.Property,
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
        .map {
            Property(
                "${it.simpleName.asString()}Internal",
                it,
                it.getAnnotationsByType(ArcticTern.Property::class).first(),
            )
        }
        .toList()

    /**
     * Applies the lazy shared preferences
     */
    override fun applyAll(builder: TypeSpec.Builder): TypeSpec.Builder {
        return builder.addProperties(
            properties.map {
                listOfNotNull(
                    buildLazyPreferenceProperty(it),
                    buildPropertyOverride(it.name, it.declaration),
                    buildIsSetProperty(it.name, it.declaration),
                    if (it.annotation.withFlow) {
                        buildFlowProperty(it.name, it.declaration)
                    } else {
                        null
                    },
                )
            }.flatten(),
        ).addFunctions(
            properties.map {
                val list = if (it.annotation.withFlow) {
                    mutableListOf(
                        buildStateFlowFunction(it.name, it.declaration),
                        buildSharedFlowFunction(it.name, it.declaration),
                        buildFlowCollectorFunction(it.name, it.declaration),

                    )
                } else {
                    mutableListOf()
                }
                if (it.annotation.withDelete) {
                    list.add(buildDeleteFunction(it.name, it.declaration))
                }
                list
            }.flatten(),
        )
    }

    /**
     * Builds the private lazy ArcticTernPreference property
     *
     * example:
     * private val counterInternal: Preference<Int> by lazy {
     *     Preference(
     *         key = "key_counter",
     *         defaultValue = super.counter,
     *         valueProvider = { preference, key, defaultValue ->
     *             preference.getInt(key, defaultValue)
     *         },
     *         valueSetter = { preference, key, value ->
     *             preference.edit().putInt(key, value).commit()
     *         },
     *         sharedPreferences = sharedPreferences
     *     )
     * }
     */
    private fun buildLazyPreferenceProperty(property: Property): PropertySpec {
        val preferenceName = "preference"
        val keyName = "key"
        val defaultValueName = "defaultValue"
        val valueName = "value"
        return PropertySpec.builder(
            property.name,
            preferenceField.type
                .parameterizedBy(property.declaration.let { it.typeName.toNullable(it.isNullable) }),
            KModifier.PRIVATE,
        )
            .delegate(
                CodeBlock.builder()
                    .beginControlFlow("lazy")
                    .addStatement("%L(", preferenceField.type.simpleName)
                    .addStatement("key = %S,", property.annotation.getKey(property.declaration))
                    .addStatement(
                        "defaultValue = super.%L,",
                        property.declaration.simpleName.asString(),
                    )
                    .beginControlFlow("valueProvider = ")
                    .add(
                        CodeBlock.builder()
                            .addStatement(
                                "%L, %L, %L ->",
                                preferenceName,
                                keyName,
                                defaultValueName,
                            )
                            .addStatement(
                                "$preferenceName.${property.declaration.typeName.preferenceGetter}",
                                keyName,
                                defaultValueName,
                            )
                            .build(),
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
                                valueName,
                            )
                            .addStatement(
                                "$preferenceName.edit().${property.declaration.typeName.preferenceSetter}.commit()",
                                keyName,
                                valueName,
                            )
                            .build(),
                    )
                    .endControlFlow()
                    .addStatement(",")
                    .addStatement(
                        "%L = %L",
                        sharedPreferencesField.name,
                        sharedPreferencesField.name,
                    )
                    .addStatement(")")
                    .endControlFlow()
                    .build(),
            )
            .build()
    }
}
