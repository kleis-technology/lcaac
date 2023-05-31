package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaSubstance
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class SubstanceReferenceFromPsiSubstanceRef(
    element: PsiSubstanceRef
) : PsiReferenceBase<PsiSubstanceRef>(element) {
    override fun resolve(): PsiElement? {
        return PsiTreeUtil.getParentOfType(element, LcaSubstance::class.java)
    }
}
