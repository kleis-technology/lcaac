package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

enum class UnitPrimitiveType {
    DEFINITION, PAREN, VARIABLE
}

interface PsiUnitPrimitive : PsiElement {
    fun getType(): UnitPrimitiveType {
        return node.findChildByType(LcaTypes.UNIT_DEFINITION)?.let { UnitPrimitiveType.DEFINITION }
            ?: node.findChildByType(LcaTypes.LPAREN)?.let { UnitPrimitiveType.PAREN }
            ?: UnitPrimitiveType.VARIABLE
    }

    fun getDefinition(): PsiUnitDefinition {
        return node.findChildByType(LcaTypes.UNIT_DEFINITION)?.psi as PsiUnitDefinition
    }

    fun getUnitInParen(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit
    }

    fun getRef(): PsiUnitRef {
        return node.findChildByType(LcaTypes.UNIT_REF)?.psi as PsiUnitRef
    }
}
