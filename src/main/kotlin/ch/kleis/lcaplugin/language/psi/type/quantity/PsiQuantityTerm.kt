package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiQuantityTerm : PsiElement {
    fun getFactor(): PsiQuantityFactor {
        return node.findChildByType(LcaTypes.QUANTITY_FACTOR)?.psi as PsiQuantityFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTypes.STAR)?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTypes.SLASH)?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiQuantityTerm? {
        return node.findChildByType(LcaTypes.QUANTITY_TERM)?.psi as PsiQuantityTerm?
    }
}
