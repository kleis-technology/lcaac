package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateSpec
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaFromProcessRefAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiProcessTemplateSpec) {
            return
        }

        val target = element.reference.resolve()
        if (target == null || target !is PsiProcess) {
            val name = element.name
            holder.newAnnotation(HighlightSeverity.WARNING, "unresolved process $name")
                .range(element)
                .highlightType(ProblemHighlightType.WARNING)
                .create()
        }
    }
}
