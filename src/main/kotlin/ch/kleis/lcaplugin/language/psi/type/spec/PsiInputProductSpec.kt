package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.reference.OutputProductReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaProcessTemplateSpec
import com.intellij.psi.util.PsiTreeUtil

interface PsiInputProductSpec : PsiUIDOwner {
    override fun getReference(): OutputProductReference {
        return OutputProductReference(this)
    }

    fun getProcessTemplateSpec(): LcaProcessTemplateSpec? {
        return PsiTreeUtil.getChildOfType(this, LcaProcessTemplateSpec::class.java)
    }
}
