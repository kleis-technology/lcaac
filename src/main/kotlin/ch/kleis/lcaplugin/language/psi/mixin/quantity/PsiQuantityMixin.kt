package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantity
