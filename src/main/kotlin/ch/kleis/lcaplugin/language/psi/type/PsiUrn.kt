package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.lib.registry.Namespace
import ch.kleis.lcaplugin.lib.registry.URN
import com.intellij.psi.PsiElement

interface PsiUrn : PsiElement {
    fun getUrn(rootNs: Namespace): URN
    fun getLocalUrn(): URN
}
