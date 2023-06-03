package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiLabelRef
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class LabelReference(
    element: PsiLabelRef
) : PsiReferenceBase<PsiLabelRef>(element), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results.first().element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return when {
            isInsideMatchLabels(element) -> resolveProcess()?.getLcaLabels()
                ?.flatMap { it.labelAssignmentList }
                ?.filter { it.name == element.name }
                ?.map { PsiElementResolveResult(it) }
                ?.toTypedArray()
                ?: emptyArray()

            isInsideBlockLabels(element) -> getEnclosingLabelAssignment()
                ?.let { arrayOf(PsiElementResolveResult(it)) }
                ?: emptyArray()

            else -> emptyArray()
        }
    }

    private fun getEnclosingLabelAssignment() = PsiTreeUtil.getParentOfType(element, LcaLabelAssignment::class.java)

    private fun isInsideMatchLabels(element: PsiLabelRef): Boolean {
        return PsiTreeUtil.getParentOfType(element, LcaMatchLabels::class.java) != null
    }

    private fun isInsideBlockLabels(element: PsiLabelRef): Boolean {
        return PsiTreeUtil.getParentOfType(element, LcaLabels::class.java) != null
    }

    private fun findTemplateSpec(): LcaProcessTemplateSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaProcessTemplateSpec::class.java)
    }

    private fun resolveProcess(): LcaProcess? {
        return findTemplateSpec()?.reference?.resolve() as LcaProcess?
    }
}
