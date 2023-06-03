package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaInputProductSpec
import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class OutputProductReferenceFromPsiProductRef(
    element: PsiProductRef
) : PsiReferenceBase<PsiProductRef>(element) {
    override fun resolve(): PsiElement? {
        return getEnclosingInputProductSpec(element)?.reference?.resolve()
            ?: getEnclosingOutputProductSpec(element)
    }
    
    private fun getEnclosingInputProductSpec(element: PsiProductRef): LcaInputProductSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaInputProductSpec::class.java)
    }

    private fun getEnclosingOutputProductSpec(element: PsiProductRef): LcaOutputProductSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaOutputProductSpec::class.java)
    }
}
