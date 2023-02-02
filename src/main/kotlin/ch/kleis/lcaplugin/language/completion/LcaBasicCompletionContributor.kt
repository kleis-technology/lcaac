package ch.kleis.lcaplugin.language.completion

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/*
    https://plugins.jetbrains.com/docs/intellij/code-completion.html#contributor-based-completion
    https://plugins.jetbrains.com/docs/intellij/element-patterns.html
 */

class LcaBasicCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(LcaTypes.IDENTIFIER)
                .inside(LcaFile::class.java),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    listOf(
                        "package", "process", "products", "inputs",
                        "emissions", "resources", "meta",
                        "substance", "factors"
                    ).map { LookupElementBuilder.create(it) }
                        .forEach {
                            result.addElement(it)
                        }
                }
            }
        )
    }
}
