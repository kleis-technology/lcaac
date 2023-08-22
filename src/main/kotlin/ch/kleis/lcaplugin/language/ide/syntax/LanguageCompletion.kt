package ch.kleis.lcaplugin.language.ide.syntax

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiErrorElement


class LanguageCompletion : CompletionContributor() {


    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)

        if (parameters.position.parent is PsiErrorElement) {
            val parent = parameters.position.parent as PsiErrorElement
            result.addElements(*extractKeyWordFromError(parent).toTypedArray())
        }
    }


    private fun extractKeyWordFromError(elt: PsiErrorElement): List<String> {
        val grp = listOfKeywordPattern.find(elt.errorDescription)?.groupValues
        return if (grp?.size == 2) {
            val errors = grp[1]
            keywordsPattern.findAll(errors)
                .map { it.groupValues }
                .filter { it.size >= 2 }
                .map { it[1] }
                .filter { it in keywordWhiteList }
                .toList()
        } else {
            listOf()
        }
    }

    private val keywordWhiteList =
        hashSetOf(
            "meta", // All Blocks
            "unit", "process", "substance", "import", "package", "variables", // Root
            "name", "type", "compartment", "sub_compartment", "reference_unit", "impacts", // Substance block
            "Emission", "Resource", "Land_use", // Substance types
            "description", "author", "other", // Meta default keys
            "reference_unit", "symbol", "dimension", "alias_for", // Unit block
            "variables", "params", "labels", // Process Block
            "products", "inputs", "resources", "emissions", "land_use", "impacts" // Process SubBlocks
        )
    private val listOfKeywordPattern = Regex("(LcaTokenType.*) expected, got")
    private val keywordsPattern = Regex("LcaTokenType\\.([^ ,]*)(, | or |)")

    private fun CompletionResultSet.addElements(vararg strings: String) {
        strings.forEach { this.addElement(LookupElementBuilder.create(it)) }
    }


}