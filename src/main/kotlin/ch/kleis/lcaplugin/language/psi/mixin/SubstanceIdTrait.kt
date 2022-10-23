package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

sealed interface SubstanceIdTrait {

    fun getNode() : ASTNode

    fun getName() : String? {
        return (getNameIdentifier() as PsiNamedElement?)?.name
    }

    fun setName(name: String): PsiElement {
        throw NotImplementedError()
    }

    fun getNameIdentifier(): PsiElement? {
        return getNode().findChildByType(ch.kleis.lcaplugin.psi.LcaTypes.SUBSTANCE_ID)?.psi
    }
}
