package com.github.albanseurat.lcaplugin.language.psi

import com.intellij.psi.PsiElement
import javax.measure.Unit

interface PsiUnitElement : PsiElement {

    fun getQuantityUnit() : Unit<*>
}