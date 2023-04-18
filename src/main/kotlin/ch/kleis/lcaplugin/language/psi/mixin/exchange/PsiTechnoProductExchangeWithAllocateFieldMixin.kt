package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchangeWithAllocateField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTechnoProductExchangeWithAllocateFieldMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiTechnoProductExchangeWithAllocateField
