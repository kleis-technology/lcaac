package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiCoreExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiCoreExpressionMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiCoreExpression
