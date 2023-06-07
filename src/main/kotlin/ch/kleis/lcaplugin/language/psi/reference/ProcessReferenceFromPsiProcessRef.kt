package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaProcessTemplateSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class ProcessReferenceFromPsiProcessRef(
    element: PsiProcessRef,
) : PsiReferenceBase<PsiProcessRef>(element) {
    override fun resolve(): PsiElement? {
        return getEnclosingProcessTemplateSpec(element)?.reference?.resolve()
            ?: getEnclosingProcess(element)
    }

    private fun getEnclosingProcessTemplateSpec(element: PsiProcessRef): LcaProcessTemplateSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaProcessTemplateSpec::class.java)
    }

    private fun getEnclosingProcess(element: PsiProcessRef): LcaProcess? {
        return PsiTreeUtil.getParentOfType(element, LcaProcess::class.java)
            ?.takeIf { it.name == element.name }
    }

    override fun getVariants(): Array<Any> {
        return getEnclosingProcessTemplateSpec(element)?.reference?.variants
            ?: emptyArray()
    }
}
