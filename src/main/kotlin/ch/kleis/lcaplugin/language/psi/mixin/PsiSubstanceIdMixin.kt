package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiSubstanceId
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

abstract class PsiSubstanceIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceId {
    override fun getSubstance(): PsiUniqueId {
        val uniqueIds = node.getChildren(TokenSet.create(LcaTypes.UNIQUE_ID))
            .map { it.psi as PsiUniqueId }
        return uniqueIds[0]
    }

    override fun getCompartment(): PsiUniqueId? {
        val uniqueIds = node.getChildren(TokenSet.create(LcaTypes.UNIQUE_ID))
            .map { it.psi as PsiUniqueId }
        return uniqueIds.getOrNull(1)
    }

    override fun getSubcompartment(): PsiUniqueId? {
        val uniqueIds = node.getChildren(TokenSet.create(LcaTypes.UNIQUE_ID))
            .map { it.psi as PsiUniqueId }
        return uniqueIds.getOrNull(2)
    }

    override fun getName(): String? {
        return listOfNotNull(getSubstance(), getCompartment(), getSubcompartment())
            .map { it.name }
            .joinToString(", ")
    }

    override fun setName(name: String): PsiElement {
        throw UnsupportedOperationException()
    }
}
