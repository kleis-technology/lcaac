package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiImpactExchangeMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiImpactExchange
