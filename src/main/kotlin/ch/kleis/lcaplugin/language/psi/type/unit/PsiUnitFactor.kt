package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiUnitFactor : PsiElement {
    fun getPrimitive(): PsiUnitPrimitive {
        return node.findChildByType(LcaTypes.UNIT_PRIMITIVE)?.psi as PsiUnitPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }
    }

}
