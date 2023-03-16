package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiFromProcessConstraint : PsiElement {
    fun getProcessTemplateRef(): PsiProcessTemplateRef {
        return node.findChildByType(LcaTypes.PROCESS_TEMPLATE_REF)?.psi as PsiProcessTemplateRef
    }

    fun getArguments(): Map<String, PsiQuantity> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .associate { it.getUid().name!! to it.getValue() }
    }
}
