package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.isAssignementReciever
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaAssignmentAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiDataRef && isAssignementReciever(element)) {
            when {
                Prelude.unitMap[element.name] != null ->
                    annotateErrWithMessage(element, holder, "Quantity reference ${element.name} is already defined in the unit prelude.")

                element.reference.multiResolve(false).size > 1 ->
                    annotateErrWithMessage(element, holder, "This name is already defined somewhere else")
            }
        }
    }
}