package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.psi.LcaDataExpression
import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiTechnoProductExchange : PsiNameIdentifierOwner {
    fun getOutputProductSpec(): LcaOutputProductSpec {
        return PsiTreeUtil.getChildOfType(this, LcaOutputProductSpec::class.java) as LcaOutputProductSpec
    }

    fun getQuantity(): LcaDataExpression {
        return PsiTreeUtil.getChildOfType(this, LcaDataExpression::class.java)!!
    }

    override fun getName(): String {
        return getOutputProductSpec().name
    }

    override fun setName(name: String): PsiElement {
        getOutputProductSpec().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getOutputProductSpec().nameIdentifier
    }
}
