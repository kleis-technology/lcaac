package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiUniqueIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUniqueId {
    override fun getName(): String {
        return this.firstChild.text //.trim('"')
    }

    override fun setName(name: String): PsiElement {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "UniqueId(${this.name})"
    }
}
