package com.tompee.arctictern.compiler

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.tompee.arctictern.compiler.entities.Field

internal class PreferenceWriter(private val classDeclaration: KSClassDeclaration) {

    companion object {

        /**
         * File and class name
         */
        private const val filename = "Preference"

        /**
         * Type parameter name
         */
        private val typeName = TypeVariableName("T")

        /**
         * Key field
         */
        private val keyField = Field("key", STRING)

        /**
         * Default value field
         */
        private val defaultValueField = Field("defaultValue", typeName)

        /**
         * Value provider field
         */
        private val valueProviderField = Field(
            "valueProvider",
            LambdaTypeName.get(
                parameters = arrayOf(Constants.sharedPreferencesField.type),
                returnType = typeName
            )
        )

        /**
         * Value setter field
         */
        private val valueSetterField = Field(
            "valueSetter",
            LambdaTypeName.get(
                parameters = arrayOf(Constants.sharedPreferencesField.type, typeName),
                returnType = UNIT
            )
        )

        /**
         * Value field
         */
        private val valueField = Field("value", typeName)

        /**
         * Is set field
         */
        private val isSetField = Field("isSet", BOOLEAN)

        /**
         * Flow type
         */
        private val flowType = ClassName("kotlinx.coroutines.flow", "Flow")
            .parameterizedBy(typeName)
    }

    fun createFileSpec(): FileSpec {
        return FileSpec.builder(classDeclaration.packageName.asString(), filename)
            .addType(buildClass())
            .addImport(
                "kotlinx.coroutines.flow",
                "callbackFlow",
                "filter",
                "onStart",
                "map",
                "conflate"
            )
            .addImport("kotlinx.coroutines.channels", "awaitClose")
            .build()
    }

    private fun buildClass(): TypeSpec {
        return TypeSpec.classBuilder(filename)
            .addModifiers(KModifier.INTERNAL)
            .addOriginatingKSFile(classDeclaration.containingFile!!)
            .addTypeVariable(typeName)
            .primaryConstructor(buildConstructor())
            .addProperties(buildPropertyList())
            .addFunctions(buildFunctionList())
            .build()
    }

    private fun buildConstructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter(keyField.toParameterSpec())
            .addParameter(defaultValueField.toParameterSpec())
            .addParameter(valueProviderField.toParameterSpec())
            .addParameter(valueSetterField.toParameterSpec())
            .addParameter(Constants.sharedPreferencesField.toParameterSpec())
            .build()
    }

    private fun buildPropertyList(): List<PropertySpec> {
        return listOf(
            keyField.toPropertySpec(true),
            defaultValueField.toPropertySpec(true),
            valueProviderField.toPropertySpec(true, KModifier.PRIVATE),
            valueSetterField.toPropertySpec(true, KModifier.PRIVATE),
            Constants.sharedPreferencesField.toPropertySpec(true, KModifier.PRIVATE),
            valueField.toPropertySpecBuilder()
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "return %L.invoke(%L)",
                            valueProviderField.name,
                            Constants.sharedPreferencesField.name
                        )
                        .build()
                )
                .build(),
            isSetField.toPropertySpecBuilder()
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "return %L.contains(%L)",
                            Constants.sharedPreferencesField.name,
                            keyField.name
                        )
                        .build()
                )
                .build()
        )
    }

    private fun buildFunctionList(): List<FunSpec> {
        return listOf(
            FunSpec.builder("set")
                .addParameter("value", typeName)
                .addStatement(
                    "%L.invoke(%L, value)",
                    valueSetterField.name,
                    Constants.sharedPreferencesField.name
                )
                .build(),
            FunSpec.builder("delete")
                .addStatement(
                    "%L.edit().remove(%L).apply()",
                    Constants.sharedPreferencesField.name,
                    keyField.name
                )
                .build(),
            FunSpec.builder("observe")
                .returns(flowType)
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("return callbackFlow {")
                        .beginControlFlow("val listener = SharedPreferences.OnSharedPreferenceChangeListener")
                        .addStatement("_, key -> trySend(key)")
                        .endControlFlow()
                        .addStatement("sharedPreferences.registerOnSharedPreferenceChangeListener(listener)")
                        .addStatement("awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }")
                        .endControlFlow()
                        .addStatement(".filter { it == key || it == null }")
                        .addStatement(".onStart { emit(\"\") } // Just to trigger the first emission")
                        .addStatement(".map { value }")
                        .addStatement(".conflate()")
                        .build()
                )
                .build()
        )
    }
}
