package com.tompee.arctictern.compiler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
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
import com.squareup.kotlinpoet.ksp.toTypeName
import com.tompee.arctictern.compiler.extensions.capitalize
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.isSupportedType
import com.tompee.arctictern.compiler.extensions.preferenceGetter
import com.tompee.arctictern.compiler.extensions.preferenceSetter
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.compiler.extensions.typeName
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
            if (!it.typeName.isSupportedType) {
                throw ProcessingException("Unsupported type ${it.typeName}", it)
            }
        }
        .map { it to it.getAnnotationsByType(ArcticTern.Property::class).first() }
        .toList()

    private data class ObjectProperty(
        val prop: KSPropertyDeclaration,
        val annotation: ArcticTern.ObjectProperty,
        val serializer: KSClassDeclaration,
        val isObject: Boolean
    )

    /**
     * Annotated object properties
     */
    private val objectProperties = classDeclaration.getAllProperties()
        .filter { it.getAnnotationsByType(ArcticTern.ObjectProperty::class).any() }
        .map { prop ->
            if (!prop.isMutable)
                throw ProcessingException("Only var properties are allowed", prop)
            if (Modifier.OPEN !in prop.modifiers)
                throw ProcessingException("Properties must be open", prop)
            val type = prop.annotations.first {
                it.shortName.asString() == ArcticTern.ObjectProperty::class.simpleName.orEmpty()
            }.arguments[0].value as KSType
            val declaration = type.declaration as? KSClassDeclaration
                ?: throw ProcessingException("Serializer provided is not a class", prop)
            if (declaration.isAbstract()) {
                throw ProcessingException("Serializer should not be abstract", prop)
            }
            when (declaration.classKind) {
                ClassKind.OBJECT -> {} // Do nothing
                ClassKind.CLASS -> {
                    if (declaration.primaryConstructor?.parameters?.isNotEmpty() == true) {
                        throw ProcessingException(
                            "Serializer should have a no-arg constructor",
                            prop
                        )
                    }
                }
                else -> throw ProcessingException("Serializer is not a class or an object", prop)
            }
            declaration.getAllSuperTypes()
                .firstOrNull {
                    it.toTypeName() == ClassName(
                        "com.tompee.arctictern.nest",
                        "Serializer"
                    ).parameterizedBy(prop.typeName)
                }
                ?: throw ProcessingException("Serializer type is not compatible", prop)
            val annotation = prop.getAnnotationsByType(ArcticTern.ObjectProperty::class).first()
            ObjectProperty(
                prop,
                annotation,
                declaration,
                declaration.classKind == ClassKind.OBJECT
            )
        }
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
            properties.map { (prop, annotation) ->
                val internalPropName = "${prop.simpleName.asString()}Internal"
                listOfNotNull(
                    buildLazyPreferenceProperty(internalPropName, prop, annotation),
                    buildPropertyOverride(internalPropName, prop),
                    if (annotation.withFlow)
                        buildFlowProperty(internalPropName, prop)
                    else null
                )
            }.flatten()
        )
            .addProperties(
                objectProperties.map {
                    val internalPropName = "${it.prop.simpleName.asString()}Internal"
                    listOfNotNull(
                        buildLazyPreferenceProperty(internalPropName, it),
                        buildPropertyOverride(internalPropName, it),
                        if (it.annotation.withFlow)
                            buildFlowProperty(internalPropName, it)
                        else null
                    )
                }.flatten()
            )
            .addFunctions(
                properties.mapNotNull { (propDeclaration, property) ->
                    val internalPropName = "${propDeclaration.simpleName.asString()}Internal"
                    if (property.withDelete) buildDeleteFunction(
                        internalPropName,
                        propDeclaration
                    ) else null
                }
            )
            .addFunctions(
                objectProperties.mapNotNull {
                    val internalPropName = "${it.prop.simpleName.asString()}Internal"
                    if (it.annotation.withDelete) buildDeleteFunction(internalPropName, it)
                    else null
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
                propertyDeclaration.typeName.toNullable(
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
                                "$preferenceName.${propertyDeclaration.typeName.preferenceGetter}",
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
                                "$preferenceName.edit().${propertyDeclaration.typeName.preferenceSetter}.apply()",
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
     * Builds the private lazy object ArcticTernPreference property
     */
    private fun buildLazyPreferenceProperty(
        internalPropName: String,
        property: ObjectProperty
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
                                if (property.isObject) "val serializer = %L" else "val serializer = %L()",
                                property.serializer.qualifiedName?.asString().orEmpty()
                            )
                            .addStatement(
                                "val stringifiedDefaultValue = serializer.serialize(%L)",
                                defaultValueName
                            )
                            .addStatement(
                                "val stringifiedValue = $preferenceName.getString(%L, stringifiedDefaultValue).orEmpty()",
                                keyName
                            )
                            .addStatement("serializer.deserialize(stringifiedValue)")
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
                                if (property.isObject) "val serializer = %L" else "val serializer = %L()",
                                property.serializer.qualifiedName?.asString().orEmpty()
                            )
                            .addStatement(
                                "val stringifiedValue = serializer.serialize(%L)",
                                valueName
                            )
                            .addStatement(
                                "$preferenceName.edit().putString(%L, stringifiedValue).apply()",
                                keyName
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
            propertyDeclaration.typeName.toNullable(propertyDeclaration.isNullable),
            KModifier.OVERRIDE
        )
            .mutable(true)
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(
                        ParameterSpec.builder("value", propertyDeclaration.typeName)
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
     * Builds the property override
     */
    private fun buildPropertyOverride(
        internalPropName: String,
        property: ObjectProperty
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
        propertyDeclaration: KSPropertyDeclaration
    ): PropertySpec {
        return PropertySpec.builder(
            "${propertyDeclaration.simpleName.asString()}Flow",
            flowField.type.parameterizedBy(
                propertyDeclaration.typeName.toNullable(
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
     * Builds the flow property
     */
    private fun buildFlowProperty(
        internalPropName: String,
        property: ObjectProperty
    ): PropertySpec {
        return PropertySpec.builder(
            "${property.prop.simpleName.asString()}Flow",
            flowField.type.parameterizedBy(property.prop.let { it.typeName.toNullable(it.isNullable) })
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

    /**
     * Builds the delete function
     */
    private fun buildDeleteFunction(
        internalPropName: String,
        property: ObjectProperty
    ): FunSpec {
        return FunSpec.builder("delete${property.prop.simpleName.asString().capitalize()}")
            .addStatement("%L.delete()", internalPropName)
            .build()
    }
}
