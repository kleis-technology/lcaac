package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.psi.LcaBioExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement


class LcaBioExchangeAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaBioExchange) {
            return
        }

        val reference = element.reference?.resolve()
        if (reference != null && reference is PsiSubstance) {
            val elementUnit = element.getUnitElement()
            val referenceUnit = reference.getUnitElement()
            if (!elementUnit.getUnit().dimension.equals(referenceUnit.getUnit().dimension)) {
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Unit ${elementUnit.name} does not match ${referenceUnit.name} from ${reference.name}"
                ).range(element.textRange)
                    .create()
            }
            return
        }

        element.nameIdentifier?.let {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unresolved flow : ${it.text}")
                .range(it)
                .highlightType(ProblemHighlightType.WARNING)
                .create()
        }
    }
}
