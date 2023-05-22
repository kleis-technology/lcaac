package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.psi.LcaArgument
import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiFromProcessConstraint : PsiElement {
    fun getProcessTemplateRef(): PsiProcessTemplateRef {
        return node.findChildByType(LcaTypes.PROCESS_TEMPLATE_REF)?.psi as PsiProcessTemplateRef
    }

    fun getArguments(): Map<String, LcaQuantityExpression> {
        return getPsiArguments()
            .associate { it.parameterRef.name to it.quantityExpression }
    }

    fun getPsiArguments(): Collection<LcaArgument> {
        return node.getChildren(TokenSet.create(LcaTypes.ARGUMENT))
            .map { it.psi as LcaArgument }
    }
}
