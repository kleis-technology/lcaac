package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
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

class ProductExchangeReference(
    element: PsiNamedElement,
    textRange: TextRange
) : PsiReferenceBase<PsiElement>(element, textRange), SearchTrait, PsiPolyVariantReference {

    private val processIdentifier: String

    init {
        processIdentifier = element.name!!
    }

    override fun getVariants(): Array<LookupElement> =
        StubIndex.getInstance()
            .getAllKeys(LcaStubIndexKeys.PRODUCT_EXCHANGES, element.project)
            .map { LookupElementBuilder.create("\"$it") }
            .toTypedArray()

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        this.findProductExchanges(element.project, processIdentifier)
            .map { PsiElementResolveResult(it) }.toTypedArray()
}
