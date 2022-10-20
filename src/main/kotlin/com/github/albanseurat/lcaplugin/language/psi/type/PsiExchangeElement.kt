package com.github.albanseurat.lcaplugin.language.psi.type

import com.intellij.psi.PsiNameIdentifierOwner

interface PsiExchangeElement : PsiNameIdentifierOwner {

    fun getUnitElement() : PsiUnitElement?
}