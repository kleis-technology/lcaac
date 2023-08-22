package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.ide.template.ErrorHelper.Companion.containsAllErrors
import ch.kleis.lcaplugin.ide.template.ErrorHelper.Companion.isInErrorInRootBlock
import ch.kleis.lcaplugin.ide.template.ErrorHelper.Companion.isInErrorInSubBlock
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiErrorElement


private val ALL_MANDATORY_SUB_BLOCK_KEYWORD = listOf(
    "name", "type", "compartment", "sub_compartment", "reference_unit", // Substance
    "symbol" // Unit
)

class LanguageCompletion : CompletionContributor() {


    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)

        if (isInErrorInRootBlock(parameters.position)) { // Root Block
            result.addElements("package", "import", "process", "variables", "substance", "unit")
        } else if (isInErrorInSubBlock(parameters.position)) { // Substance or Unit block
            val parent = parameters.position.parent as PsiErrorElement
            val expected = findExpectedToken(parent)
            if (expected != null && expected in ALL_MANDATORY_SUB_BLOCK_KEYWORD) {
                result.addElements(expected)
            } else if (containsAllErrors(parent, "impacts", "meta")) { // Substance optional blocks
                result.addElements("impacts", "meta")
            } else if (containsAllErrors(parent, "Emission", "Resource", "Land_use")) { // Substance.type
                result.addElements("Emission", "Resource", "Land_use")
            } else if (containsAllErrors(parent, "dimension", "alias_for")) { // Unit exclusive alternatives
                result.addElements("dimension", "alias_for")
            } else {
                // Nothing to complete
            }
        } else {
            // Nothing to complete
        }
    }

    private val expectedPattern = Regex("LcaTokenType\\.(.*) expected, got")

    private fun findExpectedToken(elt: PsiErrorElement): String? {
        val grp = expectedPattern.find(elt.errorDescription)?.groupValues
        return if (grp?.size == 2) grp[1] else null
    }

    private fun CompletionResultSet.addElements(vararg strings: String) {
        strings.forEach { this.addElement(LookupElementBuilder.create(it)) }
    }


}