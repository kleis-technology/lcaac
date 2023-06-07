package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaDataAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiDataRef) {
            return
        }

        if (doesResolve(element)) {
            return
        }

        val name = element.name
        holder.newAnnotation(HighlightSeverity.WARNING, "Unresolved quantity $name")
            .range(element)
            .highlightType(ProblemHighlightType.WARNING)
            .create()
    }

    // there should be a better way ...
    private fun doesResolve(psiDataRef: PsiDataRef): Boolean {
        return psiDataRef.reference.resolve()?.let { true }
            ?: Prelude.unitMap[psiDataRef.name]?.let { true }
            ?: false
    }
}
