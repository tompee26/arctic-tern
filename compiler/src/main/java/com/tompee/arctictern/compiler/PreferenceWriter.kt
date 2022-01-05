package com.tompee.arctictern.compiler

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.tompee.arctictern.compiler.generators.ObjectMemberGenerator
import com.tompee.arctictern.compiler.generators.StandardMemberGenerator
import com.tompee.arctictern.nest.ArcticTern
import com.tompee.arctictern.nest.DEFAULT_NAME

internal class PreferenceWriter(private val classDeclaration: KSClassDeclaration) {

    /**
     * [ArcticTern] annotation
     */
    private val arcticTern = classDeclaration.getAnnotationsByType(ArcticTern::class).first()

    /**
     * Class name
     */
    private val className = arcticTern.name.let {
        ClassName(
            classDeclaration.packageName.asString(),
            if (it == DEFAULT_NAME) "ArcticTern${classDeclaration.simpleName.asString()}"
            else it
        )
    }

    private val migratableWriter = MigratableWriter(arcticTern)
    private val standardMemberGenerator = StandardMemberGenerator(classDeclaration)
    private val objectMemberGenerator = ObjectMemberGenerator(classDeclaration)

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
        val companionBuilder = TypeSpec.companionObjectBuilder()
            .apply { migratableWriter.applyCompanion(this) }
        return TypeSpec.classBuilder(className)
            .addModifiers(listOfNotNull(classDeclaration.getVisibility().toKModifier()))
            .superclass(
                ClassName(
                    classDeclaration.packageName.asString(),
                    classDeclaration.simpleName.asString()
                )
            )
            .addSuperinterface(migratableField.type)
            .applyConstructor()
            .apply { migratableWriter.apply(this) }
            .applySharedPreferencesLazyProperty()
            .applyAllPropertiesAndFunctions()
            .addType(companionBuilder.build())
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
            PropertySpec.builder(
                sharedPreferencesField.name,
                sharedPreferencesField.type,
                KModifier.PRIVATE
            )
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
        return standardMemberGenerator.applyAll(this).apply {
            objectMemberGenerator.applyAll(this)
        }
    }
}
