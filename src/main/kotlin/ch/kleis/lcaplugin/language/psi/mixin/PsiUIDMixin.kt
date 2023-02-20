package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiUIDMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUID {
    override fun getName(): String {
        return this.firstChild.text.trim('"')
    }

    override fun setName(name: String): PsiElement {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "uid(${this.name})"
    }
}
