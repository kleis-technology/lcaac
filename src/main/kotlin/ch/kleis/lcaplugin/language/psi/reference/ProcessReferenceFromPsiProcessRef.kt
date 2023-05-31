package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class ProcessReferenceFromPsiProcessRef(
    element: PsiProcessRef,
) : PsiReferenceBase<PsiProcessRef>(element) {
    override fun resolve(): PsiElement? {
        return PsiTreeUtil.getParentOfType(element, LcaProcess::class.java)
    }
}
