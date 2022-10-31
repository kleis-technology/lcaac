package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*


class ProductReference(element: PsiNamedElement, textRange: TextRange) : PsiReferenceBase<PsiElement>(element, textRange),
    SearchTrait, PsiPolyVariantReference {

    private val processIdentifier: String

    init {
        processIdentifier = element.name!!
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return this.findProducts(element.project, processIdentifier)
            .map { PsiElementResolveResult(it) }.toTypedArray()
    }

}
