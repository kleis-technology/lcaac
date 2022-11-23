package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiFormulaExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiFormulaExpressionMixin(node: ASTNode): ASTWrapperPsiElement(node), PsiFormulaExpression {
}
