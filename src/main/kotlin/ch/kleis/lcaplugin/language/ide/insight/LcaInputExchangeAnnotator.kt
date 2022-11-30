package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.CreateProcessAction
import ch.kleis.lcaplugin.language.psi.mixin.PsiUniqueIdMixin
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.psi.LcaInputExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import tech.units.indriya.format.SimpleUnitFormat
import javax.measure.format.UnitFormat


class LcaInputExchangeAnnotator : Annotator {

    var parser: UnitFormat = SimpleUnitFormat.getInstance()
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if (element is LcaInputExchange) {
            val reference = element.reference?.resolve()
            if (reference == null || reference !is PsiProductExchange) {
                (element.nameIdentifier as PsiUniqueIdMixin?)?.let {
                    holder.newAnnotation(HighlightSeverity.WARNING, "Unresolved flow ${it.name}")
                        .range(it)
                        .highlightType(ProblemHighlightType.WARNING)
                        .withFix(CreateProcessAction(it.name, element.getUnitElement().text))
                        .create()
                }
            } else {
                val elementUnit = element.getUnitElement()
                val referenceUnit = reference.getUnitElement()
                if (elementUnit.getUnit().dimension
                        ?.equals(referenceUnit.getUnit().dimension) != true
                ) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Unit ${elementUnit.name} does not match ${referenceUnit.name} from ${reference.name}"
                    ).range(element.textRange).create()
                }
            }
        }
    }
}
