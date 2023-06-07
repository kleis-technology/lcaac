package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaSubstance
import ch.kleis.lcaplugin.psi.LcaSubstanceSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class SubstanceReferenceFromPsiSubstanceRef(
    element: PsiSubstanceRef
) : PsiReferenceBase<PsiSubstanceRef>(element) {
    override fun resolve(): PsiElement? {
        return getEnclosingSubstanceSpec(element)?.reference?.resolve()
            ?: getEnclosingSubstance(element)
    }

    private fun getEnclosingSubstanceSpec(element: PsiSubstanceRef): LcaSubstanceSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaSubstanceSpec::class.java)
    }

    private fun getEnclosingSubstance(element: PsiSubstanceRef): LcaSubstance? {
        return PsiTreeUtil.getParentOfType(element, LcaSubstance::class.java)
            ?.takeIf { it.name == element.name }
    }
}
