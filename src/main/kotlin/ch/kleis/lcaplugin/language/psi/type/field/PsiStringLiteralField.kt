package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiStringLiteralField : PsiElement {
    fun getValue(): String {
        return node.findChildByType(LcaTypes.STRING_LITERAL)?.psi?.text?.trim('"')
            ?: ""
    }
}
