package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiBioExchange : PsiExchange {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }
}
