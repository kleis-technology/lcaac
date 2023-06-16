package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

object AnnotatorHelper {
    fun annotateWarnWithMessage(element: PsiElement, holder: AnnotationHolder, message: String): Unit =
            holder.newAnnotation(HighlightSeverity.WARNING, message)
                    .range(element)
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()

    fun annotateErrWithMessage(element: PsiElement, holder: AnnotationHolder, message: String): Unit =
            holder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(element)
                    .highlightType(ProblemHighlightType.ERROR)
                    .create()

    fun isAssignementReciever(element: PsiDataRef) =
            isUnitDefName(element) || isLeftHandSideOfGlobalAssignement(element) || isLeftHandSideOfLocalAssignement(element)

    private fun isUnitDefName(element: PsiDataRef): Boolean =
            element.parent is PsiUnitDefinition

    private fun isLeftHandSideOfGlobalAssignement(element: PsiDataRef): Boolean =
            element.parent is PsiGlobalAssignment && element.nextSibling != null

    private fun isLeftHandSideOfLocalAssignement(element: PsiDataRef): Boolean =
            element.parent is PsiAssignment && element.nextSibling != null
}