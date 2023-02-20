package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.PsiVariable
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

enum class UnitPrimitiveType {
    LITERAL, PAREN, VARIABLE
}

interface PsiUnitPrimitive : PsiElement {
    fun getType(): UnitPrimitiveType {
        return node.findChildByType(LcaTypes.UNIT_LITERAL)?.let { UnitPrimitiveType.LITERAL }
            ?: node.findChildByType(LcaTypes.LPAREN)?.let { UnitPrimitiveType.PAREN }
            ?: UnitPrimitiveType.VARIABLE
    }

    fun asLiteral(): PsiUnitLiteral? {
        return node.findChildByType(LcaTypes.UNIT_LITERAL)?.psi as PsiUnitLiteral?
    }

    fun asVariable(): PsiVariable? {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable?
    }

    fun asUnitInParen(): PsiUnit? {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
    }

}
