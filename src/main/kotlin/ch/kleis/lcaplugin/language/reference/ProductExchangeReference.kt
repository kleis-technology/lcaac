package ch.kleis.lcaplugin.language.reference

import ch.kleis.lcaplugin.compute.urn.Namespace
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.ProductExchangeKeyIndex
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class ProductExchangeReference(
    element: PsiUniqueIdOwner
) : PsiReferenceBase<PsiUniqueIdOwner>(element, element.getUniqueId()?.textRangeInParent), PsiPolyVariantReference {

    private val localName: String = element.name!!

    override fun getVariants(): Array<LookupElement> {
        return StubIndex.getInstance()
            .getAllKeys(LcaStubIndexKeys.PRODUCT_EXCHANGES, element.project)
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val file = element.containingFile as LcaFile
        val pkg = file.getPackage()

        val localMatches = ProductExchangeKeyIndex.findProductExchanges(project, localName)
        if (localMatches.isNotEmpty()) {
            return localMatches.map { PsiElementResolveResult(it) }.toTypedArray()
        }

        val localPackageCandidate = (pkg.getUrnElement().getParts() + localName).joinToString(Namespace.SEPARATOR)
        val localPackageMatches = ProductExchangeKeyIndex.findProductExchanges(project, localPackageCandidate)
        if (localPackageMatches.isNotEmpty()) {
            return localPackageMatches.map { PsiElementResolveResult(it) }.toTypedArray()
        }

        val externalExplicitCandidates = file.getImports()
            .filter { it.isNotWildcard() }
            .map { it.getUrnElement().getParts().joinToString(Namespace.SEPARATOR) }
            .filter { it.endsWith(localName) }

        val externalWildcardCandidates = file.getImports()
            .filter { it.isWildcard() }
            .map { (it.getUrnElement().getParts() + localName).joinToString(Namespace.SEPARATOR) }

        val externalCandidates = externalExplicitCandidates + externalWildcardCandidates

        return externalCandidates.flatMap { ProductExchangeKeyIndex.findProductExchanges(project, it) }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }
}
