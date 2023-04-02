package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.lang.type.TypeCheckException
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.type_checker.PsiLcaTypeChecker
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaTechnoInputExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiTechnoInputExchange) {
            val target = element.getProductRef().reference.resolve()
            if (target == null || target !is PsiTechnoProductExchange) {
                val name = element.getProductRef().name
                holder.newAnnotation(HighlightSeverity.WARNING, "unresolved product $name")
                    .range(element.getProductRef())
                    .highlightType(ProblemHighlightType.WARNING)
                    .create()
            }
            val checker = PsiLcaTypeChecker()
            try {
                checker.check(element)
            } catch (e: TypeCheckException) {
                holder.newAnnotation(HighlightSeverity.ERROR, e.message.orEmpty())
                    .range(element)
                    .highlightType(ProblemHighlightType.ERROR)
                    .create()
            }
        }
    }
}
