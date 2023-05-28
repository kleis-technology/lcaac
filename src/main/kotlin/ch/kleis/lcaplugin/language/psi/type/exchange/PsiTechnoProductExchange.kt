package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaAllocateField
import ch.kleis.lcaplugin.psi.LcaDataExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiTechnoProductExchange : PsiNameIdentifierOwner {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaTypes.PRODUCT_REF)?.psi as PsiProductRef
    }

    fun getQuantity(): LcaDataExpression {
        return PsiTreeUtil.getChildOfType(this, LcaDataExpression::class.java)!!
    }

    override fun getName(): String {
        return getProductRef().name
    }

    override fun setName(name: String): PsiElement {
        getProductRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProductRef().nameIdentifier
    }

    fun getAllocateField(): LcaAllocateField? {
        return PsiTreeUtil.getChildOfType(this, LcaAllocateField::class.java)
    }
}
