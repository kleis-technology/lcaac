package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class PsiQuantityRefManipulator : AbstractElementManipulator<PsiQuantityRef>() {
    override fun handleContentChange(element: PsiQuantityRef, range: TextRange, newContent: String?): PsiQuantityRef {
        newContent?.let { element.setName(it) }
        return element
    }
}
