package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class UnitReference(
    element: PsiUnitRef,
) : PsiReferenceBase<PsiUnitRef>(element), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        TODO("Not yet implemented")
    }

    override fun getVariants(): Array<Any> {
        TODO("Not yet implemented")
    }
}
