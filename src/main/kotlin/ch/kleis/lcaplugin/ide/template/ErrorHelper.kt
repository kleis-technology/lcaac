package ch.kleis.lcaplugin.ide.template

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement

class ErrorHelper {
    companion object {
        fun isInErrorInRootBlock(element: PsiElement?): Boolean {
            val parent = element?.parent
            return parent != null &&
                    parent is PsiErrorElement &&
                    parent.errorDescription.contains("LcaTokenType.process") &&
                    parent.errorDescription.contains("LcaTokenType.substance")
        }
    }

}