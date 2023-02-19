package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiExchange : PsiElement {
    fun getProduct(): PsiVariable {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable
    }
}
