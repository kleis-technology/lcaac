package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

// TODO: TEST ME
class LcaQuantityAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiQuantityRef) {
            val target = element.reference?.resolve()
            if (target == null) {
                val name = element.name
                holder.newAnnotation(HighlightSeverity.WARNING, "unresolved quantity $name")
                    .range(element)
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()
            }
        }
    }
}
