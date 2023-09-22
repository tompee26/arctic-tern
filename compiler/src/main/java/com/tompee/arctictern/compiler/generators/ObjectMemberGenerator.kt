package com.tompee.arctictern.compiler.generators

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.tompee.arctictern.compiler.ProcessingException
import com.tompee.arctictern.compiler.checks.assert
import com.tompee.arctictern.compiler.extensions.getAnnotation
import com.tompee.arctictern.compiler.extensions.getKey
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.compiler.extensions.typeName
import com.tompee.arctictern.compiler.preferenceField
import com.tompee.arctictern.compiler.sharedPreferencesField
import com.tompee.arctictern.nest.ArcticTern

internal class ObjectMemberGenerator(classDeclaration: KSClassDeclaration) : BaseMemberGenerator() {

    /**
     * Property associated with [ArcticTern.ObjectProperty]
     */
    private data class ObjectProperty(
        val name: String,
        val declaration: KSPropertyDeclaration,
        val annotation: ArcticTern.ObjectProperty,
        val serializer: KSClassDeclaration,
        val isObject: Boolean,
    )

    /**
     * Annotated object properties
     */
    private val objectProperties = classDeclaration.getAllProperties()
        .filter { it.getAnnotationsByType(ArcticTern.ObjectProperty::class).any() }
        .map { prop ->
            prop.assert("Only var properties are allowed") { isMutable }
            prop.assert("Properties must be open") { isOpen() }

            val type = prop.getAnnotation<ArcticTern.ObjectProperty>()
                .arguments[0].value as KSType
            val declaration = type.declaration as? KSClassDeclaration
                ?: throw ProcessingException("Serializer provided is not a class", prop)

            declaration.assert("Serializer must not be abstract") { !isAbstract() }
            declaration.assert("Serializer is not a class or an object") {
                classKind == ClassKind.CLASS || classKind == ClassKind.OBJECT
            }

            if (declaration.classKind == ClassKind.CLASS &&
                declaration.primaryConstructor?.parameters?.isNotEmpty() == true
            ) {
                throw ProcessingException("Serializer must have a no-arg constructor", declaration)
            }

            val serializerClassName = ClassName("com.tompee.arctictern.nest", "Serializer")

            val serializer = declaration.getAllSuperTypes()
                .firstOrNull { it.toClassName() == serializerClassName }
                ?: throw ProcessingException("Class does not implement Serializer", declaration)

            when (serializer.toTypeName()) {
                serializerClassName.parameterizedBy(prop.typeName),
                serializerClassName.parameterizedBy(prop.typeName.copy(true)),
                -> {
                }
                else -> {
                    throw ProcessingException("Serializer type is not compatible", declaration)
                }
            }

            val annotation = prop.getAnnotationsByType(ArcticTern.ObjectProperty::class).first()
            ObjectProperty(
                "${prop.simpleName.asString()}Internal",
                prop,
                annotation,
                declaration,
                declaration.classKind == ClassKind.OBJECT,
            )
        }
        .toList()

    /**
     * Applies the lazy shared preferences
     */
    override fun applyAll(builder: TypeSpec.Builder): TypeSpec.Builder {
        return builder.addProperties(
            objectProperties.map {
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
            objectProperties.map {
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
        ).apply {
            objectProperties.mapNotNull { it.serializer.containingFile }
                .distinct()
                .forEach { addOriginatingKSFile(it) }
        }
    }

    /**
     * Builds the private lazy object ArcticTernPreference property
     *
     * example:
     * private val boxedIntInternal: Preference<IntWrapper?> by lazy {
     *     Preference(
     *         key = "key_boxedInt",
     *         defaultValue = super.boxedInt,
     *         valueProvider = { preference, key, defaultValue ->
     *             val serializer =
     *                 com.tompee.arctictern.serializertest.IntWrapper.NullableSerializer()
     *             val stringifiedDefaultValue = serializer.serialize(defaultValue)
     *             val stringifiedValue = preference.getString(key, stringifiedDefaultValue).orEmpty()
     *             serializer.deserialize(stringifiedValue)
     *         },
     *         valueSetter = { preference, key, value ->
     *             val serializer =
     *                 com.tompee.arctictern.serializertest.IntWrapper.NullableSerializer()
     *             val stringifiedValue = serializer.serialize(value)
     *             preference.edit().putString(key, stringifiedValue).apply()
     *         },
     *         sharedPreferences = sharedPreferences
     *     )
     * }
     */
    private fun buildLazyPreferenceProperty(property: ObjectProperty): PropertySpec {
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
                                if (property.isObject) "val serializer = %L" else "val serializer = %L()",
                                property.serializer.qualifiedName?.asString().orEmpty(),
                            )
                            .addStatement(
                                "val stringifiedDefaultValue = serializer.serialize(%L)",
                                defaultValueName,
                            )
                            .addStatement(
                                "val stringifiedValue = $preferenceName.getString(%L, stringifiedDefaultValue).orEmpty()",
                                keyName,
                            )
                            .addStatement("serializer.deserialize(stringifiedValue)")
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
                                if (property.isObject) "val serializer = %L" else "val serializer = %L()",
                                property.serializer.qualifiedName?.asString().orEmpty(),
                            )
                            .addStatement(
                                "val stringifiedValue = serializer.serialize(%L)",
                                valueName,
                            )
                            .addStatement(
                                "$preferenceName.edit().putString(%L, stringifiedValue).apply()",
                                keyName,
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
