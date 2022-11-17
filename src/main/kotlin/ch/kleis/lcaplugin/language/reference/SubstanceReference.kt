package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import ch.kleis.lcaplugin.language.psi.stub.LcaSubIndexKeys.SUBSTANCES
import ch.kleis.lcaplugin.psi.LcaEmissions
import ch.kleis.lcaplugin.psi.LcaLandUse
import ch.kleis.lcaplugin.psi.LcaResources
import ch.kleis.lcaplugin.psi.impl.LcaSubstanceImpl
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.stubs.StubIndex

class SubstanceReference(
    element: PsiNamedElement,
    textRange: TextRange
) : PsiReferenceBase<PsiNamedElement>(element, textRange), SearchTrait, PsiPolyVariantReference {

    private val processIdentifier: String

    init {
        processIdentifier = element.name!!
    }

    override fun getVariants(): Array<LookupElement> =
        StubIndex.getInstance()
            .getAllKeys(SUBSTANCES, element.project)
            .map { LookupElementBuilder.create("\"${it.replace(", ","\", \"")}") } // Ugly
            .toTypedArray()

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        this.findSubstances(element.project, processIdentifier)
            .filter { isElementCompatible(it as? LcaSubstanceImpl) }
            .map { PsiElementResolveResult(it) }.toTypedArray()

    private fun isElementCompatible(substance: LcaSubstanceImpl?): Boolean =
        element.parent is LcaResources && substance.contains("resources") ||
                element.parent is LcaEmissions && substance.contains("emissions") ||
                element.parent is LcaLandUse && substance.contains("land_use")

    fun LcaSubstanceImpl?.contains(value: String): Boolean =
        this?.substanceBody?.substanceType?.text?.contains(value) == true
}
