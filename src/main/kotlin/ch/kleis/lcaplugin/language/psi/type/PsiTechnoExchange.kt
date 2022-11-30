package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.traits.PsiFormulaExpressionOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.language.psi.type.traits.PsiUnitOwner

interface PsiTechnoExchange :
    PsiUniqueIdOwner,
    PsiFormulaExpressionOwner,
    PsiUnitOwner {
    fun getContainingProcess(): PsiProcess {
        return node.psi.parent.parent as PsiProcess
    }
}
