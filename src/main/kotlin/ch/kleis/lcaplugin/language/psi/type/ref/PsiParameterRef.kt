package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ParameterReference

interface PsiParameterRef : PsiLcaRef {
    override fun getReference(): ParameterReference {
        return ParameterReference(this)
    }
}
