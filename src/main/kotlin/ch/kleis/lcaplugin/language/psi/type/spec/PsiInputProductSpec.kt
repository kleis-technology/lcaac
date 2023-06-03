package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.reference.OutputProductReferenceFromPsiInputProductSpec
import ch.kleis.lcaplugin.psi.LcaProcessTemplateSpec
import ch.kleis.lcaplugin.psi.LcaProductRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiInputProductSpec : PsiNameIdentifierOwner {
    override fun getReference(): OutputProductReferenceFromPsiInputProductSpec {
        return OutputProductReferenceFromPsiInputProductSpec(this)
    }

    fun getProcessTemplateSpec(): LcaProcessTemplateSpec? {
        return PsiTreeUtil.getChildOfType(this, LcaProcessTemplateSpec::class.java)
    }

    fun getProductRef(): LcaProductRef {
        return PsiTreeUtil.getChildOfType(this, LcaProductRef::class.java) as LcaProductRef
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
}
