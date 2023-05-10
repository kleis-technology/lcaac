package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiGlobalAssignment : StubBasedPsiElement<GlobalAssignmentStub>, PsiNameIdentifierOwner {
    fun getQuantityRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.QUANTITY_REF)?.psi as PsiQuantityRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getQuantityRef().name
    }

    override fun setName(name: String): PsiElement {
        getQuantityRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getQuantityRef().nameIdentifier
    }
}
