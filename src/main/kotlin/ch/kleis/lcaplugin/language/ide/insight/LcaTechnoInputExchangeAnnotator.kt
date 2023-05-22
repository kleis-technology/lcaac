package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaplugin.language.type_checker.PsiTypeCheckException
import ch.kleis.lcaplugin.psi.LcaTechnoInputExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaTechnoInputExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaTechnoInputExchange) {
            return
        }
        val target = element.productRef.reference.resolve()
        if (target == null || target !is PsiTechnoProductExchange) {
            val name = element.productRef.name
            holder.newAnnotation(HighlightSeverity.WARNING, "unresolved product $name")
                .range(element.productRef)
                .highlightType(ProblemHighlightType.WARNING)
                .create()
        }
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
}
