package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaUnitAnnotator  : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiUnitRef) {
            val target = element.reference?.resolve()
            if (target == null || target !is PsiUnitDefinition) {
                val name = element.name
                holder.newAnnotation(HighlightSeverity.WARNING, "unresolved unit $name")
                    .range(element)
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()
            }
        }
    }
}
