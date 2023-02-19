package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import ch.kleis.lcaplugin.language.psi.type.PsiVariable
import ch.kleis.lcaplugin.language.psi.type.field.PsiNumberField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

enum class UnitPrimitiveType {
    LITERAL, PAREN, VARIABLE
}

interface PsiUnitPrimitive : PsiElement {
    fun getType(): UnitPrimitiveType {
        return node.findChildByType(LcaTypes.UNIT_KEYWORD)?.let { UnitPrimitiveType.LITERAL }
            ?: node.findChildByType(LcaTypes.LPAREN)?.let { UnitPrimitiveType.PAREN }
            ?: UnitPrimitiveType.VARIABLE
    }

    fun getVariable(): PsiVariable? {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable?
    }

    fun getUnitInParen(): PsiUnit? {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiUnit?
    }

    fun getUid(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
    }

    fun getSymbolField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.SYMBOL_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getDimensionField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.DIM_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getScaleField(): PsiNumberField? {
        return node.findChildByType(LcaTypes.SCALE_FIELD)?.psi as PsiNumberField?
    }
}
