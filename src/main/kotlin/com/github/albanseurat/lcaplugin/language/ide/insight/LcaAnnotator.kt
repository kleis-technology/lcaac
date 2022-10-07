package com.github.albanseurat.lcaplugin.language.ide.insight

import com.github.albanseurat.lcaplugin.actions.CreateDatasetAction
import com.github.albanseurat.lcaplugin.psi.LcaInputExchange
import com.github.albanseurat.lcaplugin.psi.LcaProductExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import tech.units.indriya.format.SimpleUnitFormat
import javax.measure.format.UnitFormat


class LcaAnnotator : Annotator {

    var parser: UnitFormat = SimpleUnitFormat.getInstance()
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if (element is LcaInputExchange) {
            val reference = element.reference?.resolve()
            if (reference == null || reference !is LcaProductExchange) {
                element.nameIdentifier?.let {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved reference : ${it.text}")
                        .range(it)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .withFix(CreateDatasetAction(it.text, element.getUnitElement()?.text))
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
                        "Unit ${elementUnit?.text} does not match ${referenceUnit?.text} from ${reference.name}"
                    ).range(element.textRange)
                        .create();
                }
            }
        }
    }
}