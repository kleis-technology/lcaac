package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.tree.TokenSet

interface PsiInclude : PsiUrnOwner {
    fun getArgumentAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }
}
