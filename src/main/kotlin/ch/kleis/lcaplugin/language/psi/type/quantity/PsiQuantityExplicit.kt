package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiQuantityExplicit : PsiElement {
    fun getAmount(): Double {
        return parseDouble(node.findChildByType(LcaTypes.NUMBER)?.text!!)
    }

    fun getUnit(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit
    }
}
