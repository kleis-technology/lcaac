package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.reference.ProcessReferenceFromPsiProcessTemplateSpec
import ch.kleis.lcaplugin.psi.LcaMatchLabels
import ch.kleis.lcaplugin.psi.LcaProcessRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiProcessTemplateSpec : PsiNameIdentifierOwner {
    override fun getReference(): ProcessReferenceFromPsiProcessTemplateSpec {
        return ProcessReferenceFromPsiProcessTemplateSpec(this)
    }

    fun getMatchLabels(): LcaMatchLabels? {
        return PsiTreeUtil.getChildOfType(this, LcaMatchLabels::class.java)
    }

    fun getProcessRef(): LcaProcessRef {
        return PsiTreeUtil.getChildOfType(this, LcaProcessRef::class.java) as LcaProcessRef
    }

    override fun getName(): String {
        return getProcessRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProcessRef().nameIdentifier
    }
}
