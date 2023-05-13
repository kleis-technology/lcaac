package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiQuantityFactor : PsiElement {
    fun getScale(): Double? {
        val scale = node.findChildByType(LcaTypes.QUANTITY_FACTOR_SCALE) ?: return null
        val number = scale.findChildByType(LcaTypes.NUMBER) ?: return null
        return number.psi.text.let { parseDouble(it) }
    }

    fun getPrimitive(): PsiQuantityPrimitive {
        return node.findChildByType(LcaTypes.QUANTITY_PRIMITIVE)?.psi as PsiQuantityPrimitive
    }

    fun getExponent(): Double? {
        val exponent = node.findChildByType(LcaTypes.QUANTITY_FACTOR_EXPONENT) ?: return null
        val number = exponent.findChildByType(LcaTypes.NUMBER) ?: return null
        return number.psi.text.let { parseDouble(it) }
    }
}
