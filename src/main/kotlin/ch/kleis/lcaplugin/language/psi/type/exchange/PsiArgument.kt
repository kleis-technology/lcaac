package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiArgument : PsiNameIdentifierOwner {
    fun getParameterRef(): PsiParameterRef {
        return node.findChildByType(LcaTypes.PARAMETER_REF)?.psi as PsiParameterRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
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
