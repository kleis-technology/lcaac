package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiTechnoInputExchange : PsiExchange {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaTypes.PRODUCT_REF)?.psi as PsiProductRef
    }

    fun getFromProcessConstraint(): PsiFromProcessConstraint? {
        return node.findChildByType(LcaTypes.FROM_PROCESS_CONSTRAINT)?.psi as PsiFromProcessConstraint?
    }
}
