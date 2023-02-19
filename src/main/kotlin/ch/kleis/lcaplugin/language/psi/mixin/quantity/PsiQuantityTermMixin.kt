package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityTerm
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityTermMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityTerm
