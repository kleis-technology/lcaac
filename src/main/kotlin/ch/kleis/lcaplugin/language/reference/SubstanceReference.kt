package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.language.SearchTrait
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys.SUBSTANCES
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class SubstanceReference(
    element: PsiElement,
    substanceId: PsiUniqueId,
    textRange: TextRange
) : PsiReferenceBase<PsiElement>(element, textRange), SearchTrait, PsiPolyVariantReference {
    private val localName = substanceId.name!!

    override fun getVariants(): Array<LookupElement> =
        StubIndex.getInstance()
            .getAllKeys(SUBSTANCES, element.project)
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        this.findSubstances(element.project, localName)
            .map { PsiElementResolveResult(it) }.toTypedArray()
}
