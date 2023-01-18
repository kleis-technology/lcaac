package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.language.psi.type.PsiUnitType
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitTypeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitType {
    override fun getUnitElement(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?  ?: throw IllegalStateException()
    }
}
