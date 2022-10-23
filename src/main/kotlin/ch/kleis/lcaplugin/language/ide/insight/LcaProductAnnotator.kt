package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.CreateDatasetAction
import ch.kleis.lcaplugin.language.psi.mixin.StringLiteralMixin
import ch.kleis.lcaplugin.language.psi.type.Product
import ch.kleis.lcaplugin.psi.LcaInputExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import tech.units.indriya.format.SimpleUnitFormat
import javax.measure.format.UnitFormat


class LcaProductAnnotator : Annotator {

    var parser: UnitFormat = SimpleUnitFormat.getInstance()
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if (element is ch.kleis.lcaplugin.psi.LcaInputExchange) {
            val reference = element.reference?.resolve()
            if (reference == null || reference !is Product) {
                (element.nameIdentifier as StringLiteralMixin?)?.let {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved reference : ${it.name}")
                        .range(it)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .withFix(CreateDatasetAction(it.name!!, element.getUnitElement()?.text))
                        .create()
                }
            } else {
                val elementUnit = element.getUnitElement()
                val referenceUnit = reference.getUnitElement()
                if (elementUnit?.getQuantityUnit()?.getDimension()
                        ?.equals(referenceUnit?.getQuantityUnit()?.getDimension()) != true
                ) {
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
