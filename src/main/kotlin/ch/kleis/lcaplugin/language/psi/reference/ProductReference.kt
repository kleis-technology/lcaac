package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaplugin.language.psi.stub.TechnoProductExchangeKeyIndex
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class ProductReference(
    element: PsiProductRef
) : PsiReferenceBase<PsiProductRef>(element), PsiPolyVariantReference {
    private val file = element.containingFile as LcaFile
    private val packages = file.getImports().map { it.getPackageName() }
        .plus(file.getPackageName())

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    private fun pkgNameOf(element: PsiElement): String {
        return (element.containingFile as LcaFile).getPackageName()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return TechnoProductExchangeKeyIndex.findTechnoProductExchanges(
            element.project,
            element.name,
        )
            .filter { packages.contains(pkgNameOf(it)) }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return StubIndex.getInstance()
            .getAllKeys(LcaStubIndexKeys.TECHNO_PRODUCT_EXCHANGES, element.project)
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
    }
}
