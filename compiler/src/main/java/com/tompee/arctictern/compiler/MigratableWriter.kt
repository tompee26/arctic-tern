package com.tompee.arctictern.compiler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.tompee.arctictern.compiler.checks.assert
import com.tompee.arctictern.nest.ArcticTern

internal class MigratableWriter(
    private val annotation: ArcticTern,
    classDeclaration: KSClassDeclaration,
) {

    companion object {

        private const val VERSION_CONST_NAME = "VERSION_CODE"
        private const val VERSION_KEY_NAME = "KEY_VERSION"
        private const val VERSION_KEY_VALUE = "\"arctic.pref.key.version\""
    }

    private data class ManualMigration(
        val declaration: KSClassDeclaration,
        val name: String,
        val annotation: ArcticTern.Migration,
        val isObject: Boolean,
    )

    private val migrations = classDeclaration.declarations
        .filter { it.getAnnotationsByType(ArcticTern.Migration::class).any() }
        .mapNotNull { clazz ->
            val annotation = clazz.getAnnotationsByType(ArcticTern.Migration::class).first()
            if (annotation.version < 1) {
                throw ProcessingException(
                    "Migration version should be greater than 0",
                    clazz,
                )
            }
            val migrationClassName = ClassName("com.tompee.arctictern.nest", "Migration")
            val declaration = clazz as? KSClassDeclaration
                ?: return@mapNotNull null

            declaration.getAllSuperTypes()
                .firstOrNull { it.toClassName() == migrationClassName }
                ?: return@mapNotNull null

            declaration.assert("Migration must not be abstract") { !isAbstract() }
            declaration.assert("Migration is not a class or an object") {
                classKind == ClassKind.CLASS || classKind == ClassKind.OBJECT
            }
            ManualMigration(
                declaration,
                declaration.simpleName.asString(),
                annotation,
                declaration.classKind == ClassKind.OBJECT,
            )
        }.toList()

    fun apply(companionBuilder: TypeSpec.Builder): TypeSpec.Builder {
        return companionBuilder.applyConstants()
            .addSuperinterface(migratableField.type)
            .applyInitialize()
            .applyIsUpdated()
            .applyMigrate()
            .applyMigrations()
    }

    private fun TypeSpec.Builder.applyConstants(): TypeSpec.Builder {
        return addProperty(
            PropertySpec.builder(VERSION_CONST_NAME, INT, KModifier.PRIVATE, KModifier.CONST)
                .initializer(annotation.version.toString())
                .build(),
        ).addProperty(
            PropertySpec.builder(
                VERSION_KEY_NAME,
                STRING,
                KModifier.PRIVATE,
                KModifier.CONST,
            )
                .initializer(VERSION_KEY_VALUE)
                .build(),
        )
    }

    /**
     * Writes isUpdated implementation
     *
     * example:
     * public override fun isUpdated(context: Context): Boolean {
     *     val sharedPreferences = context.getSharedPreferences("pref_boolean", Context.MODE_PRIVATE)
     *     if (!sharedPreferences.contains(KEY_VERSION)) return false
     *     return sharedPreferences.getInt(KEY_VERSION, 0) == VERSION_CODE
     * }
     */
    private fun TypeSpec.Builder.applyIsUpdated(): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("isUpdated")
                .returns(BOOLEAN)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(contextField.toParameterSpec())
                .writeSharedPreferencesInitialization()
                .addStatement(
                    "if (!%L.contains(%L)) return false",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                )
                .addStatement(
                    "return %L.getInt(%L, 0) == %L",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                    VERSION_CONST_NAME,
                )
                .build(),
        )
    }

    /**
     * Writes initialize implementation
     *
     * example:
     * public override fun initialize(context: Context): Unit {
     *     val sharedPreferences = context.getSharedPreferences("pref_boolean", Context.MODE_PRIVATE)
     *     if (!sharedPreferences.contains(KEY_VERSION)) {
     *         sharedPreferences.edit().putInt(KEY_VERSION, 0).apply()
     *     }
     * }
     */
    private fun TypeSpec.Builder.applyInitialize(): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("initialize")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(contextField.toParameterSpec())
                .writeSharedPreferencesInitialization()
                .beginControlFlow(
                    "if (!%L.contains(%L))",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                )
                .addStatement(
                    "%L.edit().putInt(%L, %L).apply()",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                    0,
                )
                .endControlFlow()
                .build(),
        )
    }

    /**
     * Writes migrate implementation
     *
     * example:
     * public override fun migrate(context: Context): Unit {
     *     val sharedPreferences = context.getSharedPreferences("pref_boolean", Context.MODE_PRIVATE)
     *     val currentVersion = sharedPreferences.getInt(KEY_VERSION, 0)
     *     for (i in currentVersion until VERSION_CODE) {
     *         val nextVersion = i + 1
     *         migrations[nextVersion]?.forEach {
     *             it.onMigrate(nextVersion, sharedPreferences)
     *         }
     *         sharedPreferences.edit().putInt(KEY_VERSION, nextVersion).apply()
     *     }
     * }
     */
    private fun TypeSpec.Builder.applyMigrate(): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("migrate")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(contextField.toParameterSpec())
                .writeSharedPreferencesInitialization()
                .addStatement(
                    "val currentVersion = %L.getInt(%L, 0)",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                )
                .beginControlFlow("for (i in currentVersion until %L)", VERSION_CONST_NAME)
                .addStatement("val nextVersion = i + 1")
                .beginControlFlow("migrations[nextVersion]?.forEach")
                .addStatement("it.onMigrate(nextVersion, %L)", sharedPreferencesField.name)
                .endControlFlow()
                .addStatement(
                    "%L.edit().putInt(%L, nextVersion).apply()",
                    sharedPreferencesField.name,
                    VERSION_KEY_NAME,
                )
                .endControlFlow()
                .build(),
        )
    }

    private fun TypeSpec.Builder.applyMigrations(): TypeSpec.Builder {
        val migrationName = ClassName("com.tompee.arctictern.nest", "Migration")
        val linkedHashMapType = LinkedHashMap::class.asTypeName()
            .parameterizedBy(INT, LinkedHashSet::class.asTypeName().parameterizedBy(migrationName))

        return addProperty(
            PropertySpec.builder("migrations", linkedHashMapType, KModifier.PRIVATE)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .beginControlFlow("%L().apply", linkedHashMapType)
                        .apply {
                            migrations.forEach {
                                addStatement(
                                    "insert(%L, %L)",
                                    it.annotation.version,
                                    if (it.isObject) it.name else "${it.name}()",
                                )
                            }
                        }
                        .endControlFlow()
                        .endControlFlow()
                        .build(),
                )
                .build(),
        ).addFunction(
            FunSpec.builder("insert")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("key", INT)
                .addParameter("migration", migrationName)
                .receiver(linkedHashMapType)
                .beginControlFlow("val set = getOrPut(key)")
                .addStatement("LinkedHashSet()")
                .endControlFlow()
                .addStatement("set.add(migration)")
                .build(),
        )
    }

    private fun FunSpec.Builder.writeSharedPreferencesInitialization(): FunSpec.Builder {
        return addStatement(
            "val %L = %L.getSharedPreferences(%S, Context.MODE_PRIVATE)",
            sharedPreferencesField.name,
            contextField.name,
            annotation.preferenceFile,
        )
    }
}
