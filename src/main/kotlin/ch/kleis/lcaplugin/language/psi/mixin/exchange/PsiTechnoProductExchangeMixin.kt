package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTechnoProductExchangeMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiTechnoProductExchange
