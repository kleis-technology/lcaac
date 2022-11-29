package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class ProductExchangeReference(
    element: PsiNamedElement,
    textRange: TextRange
) : PsiReferenceBase<PsiElement>(element, textRange), SearchTrait, PsiPolyVariantReference {

    private val localName: String = element.name!!

    override fun getVariants(): Array<LookupElement> =
        StubIndex.getInstance()
            .getAllKeys(LcaStubIndexKeys.PRODUCT_EXCHANGES, element.project)
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        this.findProductExchanges(element.project, localName)
            .map { PsiElementResolveResult(it) }.toTypedArray()
}
