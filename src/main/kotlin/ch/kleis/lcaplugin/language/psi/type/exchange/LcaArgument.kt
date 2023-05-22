package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiArgument : PsiNameIdentifierOwner {
    fun getParameterRef(): PsiParameterRef {
        return node.findChildByType(LcaTypes.PARAMETER_REF)?.psi as PsiParameterRef
    }

    fun getValue(): LcaQuantityExpression {
        return PsiTreeUtil.getChildOfType(this, LcaQuantityExpression::class.java)!!
    }

    override fun getName(): String {
        return getParameterRef().name
    }

    override fun setName(name: String): PsiElement {
        getParameterRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getParameterRef().nameIdentifier
    }
}
