package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiParameters : PsiElement {
    fun getAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }

    fun getEntries(): Collection<Pair<String, LcaQuantityExpression>> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getQuantityRef().name, it.getValue()) }
    }
}
