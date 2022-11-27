package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.psi.LcaTypes
import java.lang.Double.parseDouble

interface PsiFactorExchange : PsiUniqueIdOwner {
    fun getAmount(): Double {
        val number = node.findChildByType(LcaTypes.NUMBER)?.text ?: throw IllegalStateException()
        return parseDouble(number)
    }
}
