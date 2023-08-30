package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.isAssignmentReceiver
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaAssignmentAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiDataRef && isAssignmentReceiver(element)) {
            if (element.reference.multiResolve(false).size > 1) {
                annotateErrWithMessage(element, holder, "This name is already defined")
            }
        }
    }
}
