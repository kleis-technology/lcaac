package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTechnoInputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiTechnoInputExchange
