package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiVariables: PsiElement {
    fun getAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getUID().name, it.getValue()) }
    }
}
