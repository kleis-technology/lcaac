package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.reference.ProcessReferenceFromPsiProcessTemplateSpec
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaMatchLabels
import com.intellij.psi.util.PsiTreeUtil

interface PsiProcessTemplateSpec : PsiUIDOwner {
    override fun getReference(): ProcessReferenceFromPsiProcessTemplateSpec {
        return ProcessReferenceFromPsiProcessTemplateSpec(this)
    }

    fun getMatchLabels(): LcaMatchLabels? {
        return PsiTreeUtil.getChildOfType(this, LcaMatchLabels::class.java)
    }

    fun getMatchLabelsMap(): Map<String, String> {
        return getMatchLabels()
            ?.labelSelectorList
            ?.associate { it.labelRef.name to it.dataExpression.text.trim('"') }
            ?: emptyMap()
    }
}
