package ch.kleis.lcaplugin.ide.template

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType

class ErrorHelper {
    companion object {
        fun isInErrorInRootBlock(element: PsiElement?): Boolean {
            val parent = element?.parent
            return parent != null &&
                    parent is PsiErrorElement &&
                    containsAllErrors(parent, "process", "substance")
        }

        fun isInErrorInSubBlock(element: PsiElement?): Boolean {
            val parent = element?.parent
            return element.elementType == LcaTypes.IDENTIFIER &&
                    parent != null &&
                    parent is PsiErrorElement &&
                    parent.parent != null &&
                    parent.parent is PsiFile
        }

        fun containsAllErrors(elt: PsiErrorElement, vararg strings: String): Boolean {
            return strings.all { elt.errorDescription.contains("LcaTokenType.${it}") }
        }
    }

}