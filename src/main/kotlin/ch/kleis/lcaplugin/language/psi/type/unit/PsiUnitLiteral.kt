package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import ch.kleis.lcaplugin.language.psi.type.field.PsiNumberField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiUnitLiteral : PsiElement {
    fun getUid(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
    }

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
