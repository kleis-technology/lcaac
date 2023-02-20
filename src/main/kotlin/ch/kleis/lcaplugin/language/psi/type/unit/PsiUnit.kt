package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiUnit : PsiElement {
    fun getFactor(): PsiUnitFactor {
        return node.findChildByType(LcaTypes.UNIT_FACTOR)?.psi as PsiUnitFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTypes.STAR)?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTypes.SLASH)?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiUnit? {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
    }
}
