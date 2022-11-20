package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiUnitOwner : PsiElement {
    fun getUnitElement(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
            ?: throw IllegalStateException()
    }
}
