package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.StubBasedPsiElement

interface PsiGlobalAssignment : StubBasedPsiElement<GlobalAssignmentStub>, PsiUIDOwner {
    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }
}
