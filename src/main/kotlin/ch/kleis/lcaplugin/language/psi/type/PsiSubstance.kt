package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockImpacts
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

    fun hasImpacts(): Boolean {
        return node.findChildByType(LcaTypes.BLOCK_IMPACTS) != null
    }
    fun getImpacts(): PsiBlockImpacts? {
        return node.findChildByType(LcaTypes.BLOCK_IMPACTS)?.psi as PsiBlockImpacts?
    }
}
