package ch.kleis.lcaplugin.language.psi.type

import com.intellij.psi.PsiElement

interface PsiUrn : PsiElement {
    fun getParts(): List<String>
    fun getLastPart(): String {
        return getParts().last()
    }
}
