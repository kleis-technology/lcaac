package ch.kleis.lcaplugin.language.psi.mixin.unit

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitLiteral {
    override fun getName(): String {
        return super<PsiUnitLiteral>.getName()
    }
}
