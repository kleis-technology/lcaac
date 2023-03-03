package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiSubstance: PsiElement {
    fun getUid(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
    }

    fun getNameField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.NAME_FIELD)?.psi as PsiStringLiteralField
    }

    fun getCompartmentField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField
    }

    fun getSubCompartmentField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField
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

    fun getMetaBlock(): PsiMetaBlock {
        return node.findChildByType(LcaTypes.META_BLOCK)?.psi as PsiMetaBlock
    }
}