package ch.kleis.lcaplugin.language.psi.type.ref

import com.intellij.psi.PsiPolyVariantReference

sealed interface PsiLcaRef {
    fun getReference(): PsiPolyVariantReference
}
