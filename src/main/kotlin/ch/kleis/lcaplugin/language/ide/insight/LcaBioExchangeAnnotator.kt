package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaplugin.language.type_checker.PsiTypeCheckException
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

        checkReferenceResolution(element, holder)
        checkType(element, holder)
    }

    private fun checkReferenceResolution(element: LcaBioExchange, holder: AnnotationHolder) {
        val target = element.substanceSpec.reference.resolve()
        if (target == null || target !is PsiSubstance) {
            val spec = element.substanceSpec
            holder.newAnnotation(HighlightSeverity.WARNING, "unresolved substance ${render(spec)}")
                .range(spec)
                .highlightType(ProblemHighlightType.WARNING)
                .create()
        }
    }

    private fun checkType(element: LcaBioExchange, holder: AnnotationHolder) {
        val checker = PsiLcaTypeChecker()
        try {
            checker.check(element)
        } catch (e: PsiTypeCheckException) {
            holder.newAnnotation(HighlightSeverity.ERROR, e.message.orEmpty())
                .range(element)
                .highlightType(ProblemHighlightType.ERROR)
                .create()
        }
    }

    private fun render(spec: PsiSubstanceSpec): String {
        val compartmentField = spec.getCompartmentField()?.getValue()?.let { """compartment="$it"""" }
        val subCompartmentField = spec.getSubCompartmentField()?.getValue()?.let { """sub_compartment="$it"""" }
        val args = listOfNotNull(
            compartmentField,
            subCompartmentField,
        ).joinToString()
        return if (args.isBlank()) spec.name else "${spec.name}(${args})"
    }
}
