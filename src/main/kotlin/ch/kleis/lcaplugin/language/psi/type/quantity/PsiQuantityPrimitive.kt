package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

enum class QuantityPrimitiveType {
    LITERAL, PAREN, QUANTITY_REF
}

interface PsiQuantityPrimitive : PsiElement {
    fun getType(): QuantityPrimitiveType {
        return node.findChildByType(LcaTypes.NUMBER)?.let { QuantityPrimitiveType.LITERAL }
            ?: node.findChildByType(LcaTypes.QUANTITY)?.let { QuantityPrimitiveType.PAREN }
            ?: QuantityPrimitiveType.QUANTITY_REF
    }

    fun getAmount(): Double {
        return node.findChildByType(LcaTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }!!
    }

    fun getQuantityInParen(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }

    fun getRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.QUANTITY_REF)?.psi as PsiQuantityRef
    }
}
