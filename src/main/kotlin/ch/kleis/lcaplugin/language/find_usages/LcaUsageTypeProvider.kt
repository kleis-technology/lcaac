package ch.kleis.lcaplugin.language.find_usages

import ch.kleis.lcaplugin.language.psi.type.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.PsiInputExchange
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider

class LcaUsageTypeProvider : UsageTypeProvider {
    override fun getUsageType(element: PsiElement): UsageType? {
        if (element is PsiInputExchange) {
            return LcaUsageType.TECHNOSPHERE_INPUT
        }
        if (element is PsiProductExchange) {
            return LcaUsageType.TECHNOSPHERE_PRODUCT
        }
        if (element is PsiBioExchange) {
            return LcaUsageType.BIOSPHERE
        }
        if (element is PsiSubstance) {
            return LcaUsageType.SUBSTANCE
        }
        return null
    }
}
