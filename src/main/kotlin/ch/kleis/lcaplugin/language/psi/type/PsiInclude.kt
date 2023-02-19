package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiInclude : PsiElement {
    fun getUrn(): PsiUrn {
        return node.findChildByType(LcaTypes.URN)?.psi as PsiUrn
    }

    fun getArgumentAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }
}
