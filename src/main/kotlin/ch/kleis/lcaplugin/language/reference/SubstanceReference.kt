package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*


class SubstanceReference(element: PsiNamedElement, textRange: TextRange) : PsiReferenceBase<PsiNamedElement>(element, textRange),
    SearchTrait, PsiPolyVariantReference {

    private val datasetIdentifier: String

    init {
        datasetIdentifier = element.name!!
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return this.findSubstances(element.project, datasetIdentifier)
            .map { PsiElementResolveResult(it) }.toTypedArray()
    }

}
