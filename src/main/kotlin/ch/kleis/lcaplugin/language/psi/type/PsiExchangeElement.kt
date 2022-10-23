package ch.kleis.lcaplugin.language.psi.type

import com.intellij.psi.PsiNameIdentifierOwner

interface PsiExchangeElement : PsiNameIdentifierOwner {

    fun getUnitElement() : PsiUnitElement?
}
