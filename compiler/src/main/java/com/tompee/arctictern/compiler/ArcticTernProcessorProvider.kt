package com.tompee.arctictern.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tompee.arctictern.nest.ArcticTern
import java.io.OutputStream

@AutoService(SymbolProcessorProvider::class)
class ArcticTernProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ArcticTernProcessor(environment)
    }
}

private class ArcticTernProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator: CodeGenerator = environment.codeGenerator
    private val logger: KSPLogger = environment.logger
    private val options: Map<String, String> = environment.options

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(ArcticTern::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()

        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = "com.tompee",
            fileName = "GeneratedFunctions"
        )
        file.close()
        throw IllegalStateException()
    }
}
