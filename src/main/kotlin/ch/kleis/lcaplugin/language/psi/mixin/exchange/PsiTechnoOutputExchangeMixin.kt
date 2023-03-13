package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoOutputExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTechnoOutputExchangeMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiTechnoOutputExchange
