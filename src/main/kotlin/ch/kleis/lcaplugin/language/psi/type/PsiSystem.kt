package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiSystem : PsiElement {
    fun getUid(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
    }

    fun getParameters(): Collection<PsiParameter> {
        return node.getChildren(TokenSet.create(LcaTypes.PARAMS))
            .flatMap { it.getChildren(TokenSet.create(LcaTypes.PARAM)).toList() }
            .map { it.psi as PsiParameter }
    }

    fun getLocalAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }

    fun getIncludes(): Collection<PsiInclude> {
        return node.getChildren(TokenSet.create(LcaTypes.INCLUDE))
            .map { it.psi as PsiInclude }
    }

    fun getProcesses(): Collection<PsiProcess> {
        return node.getChildren(TokenSet.create(LcaTypes.PROCESS))
            .map { it.psi as PsiProcess }
    }

    fun getSystems(): Collection<PsiSystem> {
        return node.getChildren(TokenSet.create(LcaTypes.SYSTEM))
            .map { it.psi as PsiSystem }
    }
}
