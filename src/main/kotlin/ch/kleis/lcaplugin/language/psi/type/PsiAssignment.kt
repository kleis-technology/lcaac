package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiAssignment : PsiNameIdentifierOwner {
    fun getQuantityRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.QUANTITY_REF)?.psi as PsiQuantityRef
    }

    fun getValue(): LcaQuantityExpression {
        return PsiTreeUtil.findChildrenOfType(this, LcaQuantityExpression::class.java).elementAt(1)
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
