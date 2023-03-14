package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityExplicit
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityExplicitMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityExplicit
