package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

interface PsiSubstanceId : PsiNamedElement {
    fun getSubstance(): PsiUniqueId {
        val uniqueIds = node.getChildren(TokenSet.create(LcaTypes.UNIQUE_ID))
            .map { it.psi as PsiUniqueId }
        return uniqueIds[0]
    }

    fun getCompartment(): PsiUniqueId? {
        val uniqueIds = node.getChildren(TokenSet.create(LcaTypes.UNIQUE_ID))
            .map { it.psi as PsiUniqueId }
        return uniqueIds.getOrNull(1)
    }

    fun getSubcompartment(): PsiUniqueId? {
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
