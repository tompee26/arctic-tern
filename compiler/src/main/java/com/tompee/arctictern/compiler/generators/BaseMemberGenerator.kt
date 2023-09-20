package com.tompee.arctictern.compiler.generators

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.tompee.arctictern.compiler.coroutineScopeField
import com.tompee.arctictern.compiler.extensions.capitalize
import com.tompee.arctictern.compiler.extensions.isNullable
import com.tompee.arctictern.compiler.extensions.toNullable
import com.tompee.arctictern.compiler.extensions.typeName
import com.tompee.arctictern.compiler.flowCollectorField
import com.tompee.arctictern.compiler.flowField
import com.tompee.arctictern.compiler.sharedFlowField
import com.tompee.arctictern.compiler.sharingStartedField
import com.tompee.arctictern.compiler.stateFlowField

/**
 * Base implementation of [MemberGenerator]
 */
internal abstract class BaseMemberGenerator : MemberGenerator {

    /**
     * Builds the property override
     *
     * example:
     * public override var counter: Int
     *     get() = counterInternal.value
     *     set(`value`) {
     *         counterInternal.value = value
     *     }
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildPropertyOverride(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): PropertySpec {
        return PropertySpec.builder(
            propertyDeclaration.simpleName.asString(),
            propertyDeclaration.let { it.typeName.toNullable(it.isNullable) },
            KModifier.OVERRIDE,
        )
            .mutable(true)
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(
                        ParameterSpec.builder("value", propertyDeclaration.typeName)
                            .build(),
                    )
                    .addStatement("%L.value = value", propertyName)
                    .build(),
            )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L.value", propertyName)
                    .build(),
            )
            .build()
    }

    /**
     * Builds the is set property
     *
     * example:
     * public val isCounterSet: Boolean
     *     get() = counterInternal.isSet
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildIsSetProperty(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): PropertySpec {
        return PropertySpec.builder(
            "is${propertyDeclaration.simpleName.asString().capitalize()}Set",
            BOOLEAN,
        )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L.isSet", propertyName)
                    .build(),
            )
            .build()
    }

    /**
     * Builds the flow property
     *
     * example:
     * public val counterFlow: Flow<Int>
     *     get() = counterInternal.observe()
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildFlowProperty(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): PropertySpec {
        return PropertySpec.builder(
            "${propertyDeclaration.simpleName.asString()}Flow",
            flowField.type
                .parameterizedBy(propertyDeclaration.let { it.typeName.toNullable(it.isNullable) }),
        )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L.observe()", propertyName)
                    .build(),
            )
            .build()
    }

    /**
     * Builds the state flow function
     *
     * example:
     * public fun counterAsStateFlow(scope: CoroutineScope, started: SharingStarted): StateFlow<Int> =
     *     counterInternal.asStateFlow(scope, started)
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildStateFlowFunction(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): FunSpec {
        return FunSpec.builder("${propertyDeclaration.simpleName.asString()}AsStateFlow")
            .returns(
                stateFlowField.type
                    .parameterizedBy(propertyDeclaration.let { it.typeName.toNullable(it.isNullable) }),
            )
            .addParameter(coroutineScopeField.toParameterSpec())
            .addParameter(sharingStartedField.toParameterSpec())
            .addStatement(
                "return %L.asStateFlow(%L, %L)",
                propertyName,
                coroutineScopeField.name,
                sharingStartedField.name,
            )
            .build()
    }

    /**
     * Builds the shared flow function
     *
     * example:
     * public fun counterAsSharedFlow(scope: CoroutineScope, started: SharingStarted): SharedFlow<Int> =
     *     counterInternal.asSharedFlow(scope, started)
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildSharedFlowFunction(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): FunSpec {
        return FunSpec.builder("${propertyDeclaration.simpleName.asString()}AsSharedFlow")
            .returns(
                sharedFlowField.type
                    .parameterizedBy(propertyDeclaration.let { it.typeName.toNullable(it.isNullable) }),
            )
            .addParameter(coroutineScopeField.toParameterSpec())
            .addParameter(sharingStartedField.toParameterSpec())
            .addStatement(
                "return %L.asSharedFlow(%L, %L)",
                propertyName,
                coroutineScopeField.name,
                sharingStartedField.name,
            )
            .build()
    }

    /**
     * Builds the flow collector function
     *
     * example:
     * public fun counterAsFlowCollector(): FlowCollector<Int> = counterInternal.asFlowCollector()
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildFlowCollectorFunction(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): FunSpec {
        return FunSpec.builder("${propertyDeclaration.simpleName.asString()}AsFlowCollector")
            .returns(
                flowCollectorField.type.parameterizedBy(
                    propertyDeclaration.let {
                        it.typeName.toNullable(
                            it.isNullable,
                        )
                    },
                ),
            )
            .addStatement("return %L.asFlowCollector()", propertyName)
            .build()
    }

    /**
     * Builds the delete function
     *
     * example:
     * public fun deleteCounter(): Unit {
     *     counterInternal.delete()
     * }
     *
     * @param propertyName the name of the generated property
     * @param propertyDeclaration the source property declaration
     */
    protected fun buildDeleteFunction(
        propertyName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): FunSpec {
        return FunSpec.builder("delete${propertyDeclaration.simpleName.asString().capitalize()}")
            .addStatement("%L.delete()", propertyName)
            .build()
    }
}
