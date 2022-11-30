package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiFactorExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiFactorExchangeMixin(node: ASTNode): ASTWrapperPsiElement(node), PsiFactorExchange {
    override fun getName(): String? = super<PsiFactorExchange>.getName()
}
