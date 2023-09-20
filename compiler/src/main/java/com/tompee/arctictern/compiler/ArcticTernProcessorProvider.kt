package com.tompee.arctictern.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import com.tompee.arctictern.nest.ArcticTern
import com.tompee.arctictern.nest.ArcticTernApp

@AutoService(SymbolProcessorProvider::class)
class ArcticTernProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ArcticTernProcessor(environment)
    }

    private class ArcticTernProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

        private val codeGenerator: CodeGenerator = environment.codeGenerator
        private val logger: KSPLogger = environment.logger
        private val options: Map<String, String> = environment.options

        private val filesToWrite = mutableSetOf<FileSpec>()

        override fun process(resolver: Resolver): List<KSAnnotated> {
            try {
                val appSymbol = resolver
                    .getSymbolsWithAnnotation(ArcticTernApp::class.qualifiedName.orEmpty())
                    .filterIsInstance<KSClassDeclaration>()
                    .firstOrNull()

                val annotatedTypes = resolver
                    .getSymbolsWithAnnotation(ArcticTern::class.qualifiedName.orEmpty())
                    .filterIsInstance<KSClassDeclaration>()
                    .toList()

                val preferences = annotatedTypes
                    .map { PreferenceWriter(it).createFile() }

                filesToWrite.addAll(preferences)
                if (appSymbol != null) {
                    filesToWrite += ManagerWriter(
                        appSymbol,
                        preferences.zip(annotatedTypes).toMap(),
                    ).createManager()
                }
            } catch (e: ProcessingException) {
                logger.error(e.message.orEmpty(), e.node)
            } catch (e: Throwable) {
                logger.error(e.message.orEmpty())
            }

            return emptyList()
        }

        override fun finish() {
            super.finish()
            filesToWrite.forEach {
                it.writeTo(codeGenerator, true)
            }
        }
    }
}
