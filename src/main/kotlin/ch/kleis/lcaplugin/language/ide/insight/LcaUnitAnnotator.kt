package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaUnitAnnotator  : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiUnitRef) {
            val target = tryResolve(element)
            if (target == null) {
                val name = element.name
                holder.newAnnotation(HighlightSeverity.WARNING, "unresolved unit $name")
                    .range(element)
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()
            }
        }
    }

    // there should be a better way ...
    private fun tryResolve(psiUnitRef: PsiUnitRef): Unit? {
        return psiUnitRef.reference.resolve()?.let { }
            ?: Prelude.unitMap[psiUnitRef.name]?.let { }
    }
}
