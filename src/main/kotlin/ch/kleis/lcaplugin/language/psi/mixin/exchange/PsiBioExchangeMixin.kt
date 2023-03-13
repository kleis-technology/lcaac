package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiBioExchangeMixin (node: ASTNode) : ASTWrapperPsiElement(node), PsiBioExchange
