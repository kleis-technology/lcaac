package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.language.psi.type.PsiUrn
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiUrnOwner : PsiElement {
    fun getUrnElement(): PsiUrn {
        return node.findChildByType(LcaTypes.URN)?.psi as PsiUrn?
            ?: throw IllegalStateException()
    }
}
