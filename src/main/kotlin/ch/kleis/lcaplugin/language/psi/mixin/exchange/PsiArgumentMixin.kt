package ch.kleis.lcaplugin.language.psi.mixin.exchange

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiArgument
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiArgumentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiArgument {
    override fun getName(): String {
        return super<PsiArgument>.getName()
    }
}
