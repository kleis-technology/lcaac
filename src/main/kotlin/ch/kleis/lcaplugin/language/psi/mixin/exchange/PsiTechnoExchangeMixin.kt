package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTechnoExchangeMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiTechnoExchange
