package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class SubstanceReferenceFromPsiSubstanceRef(
    element: PsiSubstanceRef
) : PsiReferenceBase<PsiSubstanceRef>(element) {
    override fun resolve(): PsiElement? {
        return PsiTreeUtil.findFirstParent(element) { it is PsiSubstance }
    }
}
