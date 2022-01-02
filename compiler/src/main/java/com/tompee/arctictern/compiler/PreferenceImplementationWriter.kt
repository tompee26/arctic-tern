package com.tompee.arctictern.compiler

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.tompee.arctictern.compiler.entities.Field
import com.tompee.arctictern.nest.ArcticTern

internal class PreferenceImplementationWriter(private val classDeclaration: KSClassDeclaration) {

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

    private val arcticTern = classDeclaration.getAnnotationsByType(ArcticTern::class).first()

    private val properties = classDeclaration.getAllProperties()
        .filter { it.getAnnotationsByType(ArcticTern.Property::class).any() }
        .toList()

    init {
        // Validate that only interface can be annotated
        if (classDeclaration.classKind != ClassKind.INTERFACE) {
            throw ProcessingException("Annotated class is not an interface", classDeclaration)
        }
    }
}
