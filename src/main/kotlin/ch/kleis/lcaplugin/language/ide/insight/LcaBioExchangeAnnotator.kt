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

        if (element is LcaBioExchange) {
            val reference = element.reference?.resolve()
            if (reference == null || reference !is PsiSubstance) {
                element.nameIdentifier?.let {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved reference : ${it.text}")
                        .range(it)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .create()
                }
            } else {
                val elementUnit = element.getUnitElement()
                val referenceUnit = reference.getUnitElement()
                if (!elementUnit.getUnit().dimension.equals(referenceUnit.getUnit().dimension)) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Unit ${elementUnit?.name} does not match ${referenceUnit?.name} from ${reference.name}"
                    ).range(element.textRange)
                        .create();
                }
            }
        }
    }
}
