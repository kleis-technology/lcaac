package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiSubstance: PsiElement {
    fun getUid(): PsiUID {
        return node.findChildByType(LcaTypes.UID)!!.psi as PsiUID
    }

    fun getReferenceUnitField(): PsiUnitField {
        return node.findChildByType(LcaTypes.REFERENCE_UNIT_FIELD)?.psi as PsiUnitField
    }

    fun hasEmissionFactors(): Boolean {
        return node.findChildByType(LcaTypes.EMISSION_FACTORS) != null
    }
    fun getEmissionFactors(): PsiEmissionFactors? {
        return node.findChildByType(LcaTypes.EMISSION_FACTORS)?.psi as PsiEmissionFactors?
    }
}
