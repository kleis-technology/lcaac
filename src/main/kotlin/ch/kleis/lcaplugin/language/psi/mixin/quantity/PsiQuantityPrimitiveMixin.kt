package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityPrimitive
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityPrimitiveMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityPrimitive
