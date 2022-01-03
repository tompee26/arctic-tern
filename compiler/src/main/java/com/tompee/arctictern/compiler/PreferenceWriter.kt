package com.tompee.arctictern.compiler

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.tompee.arctictern.compiler.extensions.capitalize
import com.tompee.arctictern.compiler.extensions.className
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.getPreferenceGetter
import com.tompee.arctictern.compiler.extensions.getPreferenceSetter
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.isSupportedType
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.nest.ArcticTern

internal class PreferenceWriter(private val classDeclaration: KSClassDeclaration) {

    /**
     * [ArcticTern] annotation
     */
    private val arcticTern = classDeclaration.getAnnotationsByType(ArcticTern::class).first()

    /**
     * Class name
     */
    private val className = ClassName(
        classDeclaration.packageName.asString(),
        "ArcticTern${classDeclaration.simpleName.asString()}"
    )

    /**
     * Annotated properties
     */
    private val properties = classDeclaration.getAllProperties()
        .filter { it.getAnnotationsByType(ArcticTern.Property::class).any() }
        .onEach {
            if (!it.isMutable)
                throw ProcessingException("Only var properties are allowed", it)
            if (Modifier.OPEN !in it.modifiers)
                throw ProcessingException("Properties must be open", it)
            if (!it.className.isSupportedType) {
                throw ProcessingException("Unsupported type ${it.className.simpleName}", it)
            }
        }
        .map { it to it.getAnnotationsByType(ArcticTern.Property::class).first() }
        .toList()

    init {
        // Validate that only interface can be annotated
        if (Modifier.ABSTRACT !in classDeclaration.modifiers) {
            throw ProcessingException("Annotated class is not an abstract class", classDeclaration)
        }
    }

    fun createFile(): FileSpec {
        return FileSpec.builder(className.packageName, className.simpleName)
            .addType(buildClass())
            .build()
    }

    /**
     * Build the class. The hierarchy will be
     * Class implements the preference file
     *   Constructor
     *   SharedPreference Property
     *   All properties declared
     *   Optional delete functions
     */
    private fun buildClass(): TypeSpec {
        return TypeSpec.classBuilder(className)
            .addModifiers(listOfNotNull(classDeclaration.getVisibility().toKModifier()))
            .superclass(
                ClassName(
                    classDeclaration.packageName.asString(),
                    classDeclaration.simpleName.asString()
                )
            )
            .applyConstructor()
            .applySharedPreferencesLazyProperty()
            .applyAllPropertiesAndFunctions()
            .build()
    }

    /**
     * Applies the constructor and properties. Contains the context as a parameter
     */
    private fun TypeSpec.Builder.applyConstructor(): TypeSpec.Builder {
        return primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(contextField.toParameterSpec())
                .build()
        ).addProperty(
            contextField
                .toPropertySpecBuilder(true, KModifier.PRIVATE)
                .build()
        )
    }

    /**
     * Applies the SharedPreferences property as a lazy delegate
     */
    private fun TypeSpec.Builder.applySharedPreferencesLazyProperty(): TypeSpec.Builder {
        return addProperty(
            PropertySpec.builder(sharedPreferencesField.name, sharedPreferencesField.type)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .addStatement(
                            "context.getSharedPreferences(%S, Context.MODE_PRIVATE)",
                            arcticTern.preferenceFile
                        )
                        .endControlFlow()
                        .build()
                )
                .build()
        )
    }

    /**
     * Applies the properties and functions. Includes:
     * - Private ArcticTernPreference property
     * - Public property that delegates to the preference
     * - Optional: Flow property
     * - Optional: Delete function
     */
    private fun TypeSpec.Builder.applyAllPropertiesAndFunctions(): TypeSpec.Builder {
        return addProperties(
            properties.map { (propDeclaration, property) ->
                val internalPropName = "${propDeclaration.simpleName.asString()}Internal"
                listOfNotNull(
                    buildLazyPreferenceProperty(internalPropName, propDeclaration, property),
                    buildPropertyOverride(internalPropName, propDeclaration),
                    if (property.withFlow)
                        buildFlowProperty(internalPropName, propDeclaration)
                    else null
                )
            }.flatten()
        ).addFunctions(
            properties.mapNotNull { (propDeclaration, property) ->
                val internalPropName = "${propDeclaration.simpleName.asString()}Internal"
                if (property.withDelete) buildDeleteFunction(
                    internalPropName,
                    propDeclaration
                ) else null
            }
        )
    }

    /**
     * Builds the private lazy ArcticTernPreference property
     */
    private fun buildLazyPreferenceProperty(
        internalPropName: String,
        propertyDeclaration: KSPropertyDeclaration,
        property: ArcticTern.Property
    ): PropertySpec {
        val preferenceName = "preference"
        val keyName = "key"
        val defaultValueName = "defaultValue"
        val valueName = "value"
        return PropertySpec.builder(
            internalPropName,
            preferenceField.type.parameterizedBy(
                propertyDeclaration.className.toNullable(
                    propertyDeclaration.isNullable
                )
            ),
            KModifier.PRIVATE
        )
            .delegate(
                CodeBlock.builder()
                    .beginControlFlow("lazy")
                    .addStatement("%L(", preferenceField.type.simpleName)
                    .addStatement("key = %S,", property.getKey(propertyDeclaration))
                    .addStatement(
                        "defaultValue = super.%L,",
                        propertyDeclaration.simpleName.asString()
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
                                "$preferenceName.${
                                propertyDeclaration.className.getPreferenceGetter(
                                    propertyDeclaration.isNullable
                                )
                                }",
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
                                "$preferenceName.edit().${
                                propertyDeclaration.className.getPreferenceSetter(
                                    propertyDeclaration.isNullable
                                )
                                }.apply()",
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
        propertyDeclaration: KSPropertyDeclaration
    ): PropertySpec {
        return PropertySpec.builder(
            propertyDeclaration.simpleName.asString(),
            propertyDeclaration.className.toNullable(propertyDeclaration.isNullable),
            KModifier.OVERRIDE
        )
            .mutable(true)
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(
                        ParameterSpec.builder("value", propertyDeclaration.className)
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
        propertyDeclaration: KSPropertyDeclaration
    ): PropertySpec {
        return PropertySpec.builder(
            "${propertyDeclaration.simpleName.asString()}Flow",
            flowField.type.parameterizedBy(
                propertyDeclaration.className.toNullable(
                    propertyDeclaration.isNullable
                )
            )
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
        propertyDeclaration: KSPropertyDeclaration
    ): FunSpec {
        return FunSpec.builder("delete${propertyDeclaration.simpleName.asString().capitalize()}")
            .addStatement("%L.delete()", internalPropName)
            .build()
    }
}
