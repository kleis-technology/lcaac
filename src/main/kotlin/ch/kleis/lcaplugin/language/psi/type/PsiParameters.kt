package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiParameters: PsiElement {
    fun getUIDs(): Collection<PsiUID> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { it.getUID() }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getUID().name, it.getValue()) }
    }
}
