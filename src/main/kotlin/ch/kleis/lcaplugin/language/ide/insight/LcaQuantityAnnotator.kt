package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaQuantityAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiQuantityRef) {
            val target = tryResolve(element)
            if (target == null) {
                val name = element.name
                holder.newAnnotation(HighlightSeverity.WARNING, "unresolved quantity $name")
                    .range(element)
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()
            }
        }
    }

    // there should be a better way ...
    private fun tryResolve(psiQuantityRef: PsiQuantityRef): Unit? {
        return psiQuantityRef.reference.resolve()?.let { }
            ?: Prelude.unitMap[psiQuantityRef.name]?.let { }
    }
}
