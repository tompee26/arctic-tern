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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.tompee.arctictern.compiler.extensions.className
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.preferenceGetter
import com.tompee.arctictern.compiler.extensions.preferenceSetter
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
        "${classDeclaration.simpleName.asString()}Impl"
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
            if (it.className !in supportedTypeMap.keys) {
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
     */
    private fun buildClass(): TypeSpec {
        return TypeSpec.classBuilder(className)
            .addModifiers(
                listOfNotNull(classDeclaration.getVisibility().toKModifier())
            )
            .superclass(
                ClassName(
                    classDeclaration.packageName.asString(),
                    classDeclaration.simpleName.asString()
                )
            )
            .primaryConstructor(buildConstructor())
            .addProperties(buildConstructorProperties())
            .addProperty(createSharedPreferencesLazyProperty())
            .addProperties(buildAllProperties())
            .build()
    }

    /**
     * Builds the constructor. Contains the context as a parameter
     */
    private fun buildConstructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter(contextField.toParameterSpec())
            .build()
    }

    /**
     * Builds the constructor parameters as properties to modify
     * their visibility
     */
    private fun buildConstructorProperties(): List<PropertySpec> {
        return listOf(
            contextField
                .toPropertySpecBuilder(true, KModifier.PRIVATE)
                .build()
        )
    }

    /**
     * Creates the SharedPreferences property as a lazy delegate
     */
    private fun createSharedPreferencesLazyProperty(): PropertySpec {
        return PropertySpec.builder(sharedPreferencesField.name, sharedPreferencesField.type)
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
    }

    /**
     * Properties
     */
    private fun buildAllProperties(): List<PropertySpec> {
        return properties.map { (propDeclaration, property) ->
            listOf(
                buildLazyPreferenceProperty(propDeclaration, property)
            )
        }.flatten()
    }

    /**
     * Builds the private lazy ArcticTernPreference property
     */
    private fun buildLazyPreferenceProperty(
        propertyDeclaration: KSPropertyDeclaration,
        property: ArcticTern.Property
    ): PropertySpec {
        val preferenceName = "preference"
        val keyName = "key"
        val defaultValueName = "defaultValue"
        val valueName = "value"
        return PropertySpec.builder(
            "${propertyDeclaration.simpleName.asString()}Internal",
            preferenceField.type.parameterizedBy(propertyDeclaration.className),
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
                                "%L.%L(%L, %L)",
                                preferenceName,
                                propertyDeclaration.className.preferenceGetter,
                                keyName,
                                defaultValueName
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
                                "%L.edit().%L(%L, %L).apply()",
                                preferenceName,
                                propertyDeclaration.className.preferenceSetter,
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
}
