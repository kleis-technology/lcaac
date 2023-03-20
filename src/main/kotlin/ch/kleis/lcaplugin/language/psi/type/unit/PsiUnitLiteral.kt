package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.field.PsiNumberField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiUnitLiteral : PsiUIDOwner {
    fun getSymbolField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.SYMBOL_FIELD)?.psi as PsiStringLiteralField
    }

    fun getDimensionField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.DIM_FIELD)?.psi as PsiStringLiteralField
    }

    fun getScaleField(): PsiNumberField {
        return node.findChildByType(LcaTypes.SCALE_FIELD)?.psi as PsiNumberField
    }
}
