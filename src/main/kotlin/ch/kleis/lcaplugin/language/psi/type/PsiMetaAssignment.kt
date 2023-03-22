package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiMetaAssignment : PsiUIDOwner {
    fun getValue(): String {
        return node.findChildByType(LcaTypes.STRING_LITERAL)?.psi?.text?.trim('"')
            ?: ""
    }
}
