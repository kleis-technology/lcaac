package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateWarnWithMessage
import ch.kleis.lcaplugin.language.type_checker.LcaMatchLabelsEvaluator
import ch.kleis.lcaplugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaplugin.language.type_checker.PsiTypeCheckException
import ch.kleis.lcaplugin.psi.LcaInputProductSpec
import ch.kleis.lcaplugin.psi.LcaOutputProductSpec
import ch.kleis.lcaplugin.psi.LcaTechnoInputExchange
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaTechnoInputExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaTechnoInputExchange) {
            return
        }
        val target = element.inputProductSpec.reference.resolve()

        if (target == null
                || target !is LcaOutputProductSpec
        ) {
            val message = errorMessage(element.inputProductSpec)
            annotateWarnWithMessage(element.inputProductSpec, holder, message)
        }
        val checker = PsiLcaTypeChecker()
        try {
            checker.check(element)
        } catch (e: PsiTypeCheckException) {
            annotateErrWithMessage(element, holder, e.message.orEmpty())
        }
    }

    private fun errorMessage(inputProductSpec: LcaInputProductSpec): String {
        val product = inputProductSpec.name
        val process = inputProductSpec.getProcessTemplateSpec()?.name
        val labels = inputProductSpec.getProcessTemplateSpec()
                ?.getMatchLabels()
                ?.let { LcaMatchLabelsEvaluator().evalOrNull(it) }
        val parts = listOfNotNull(
                product,
                process?.let { "from $it" },
                labels?.let { "match $it" },
        ).joinToString(" ")
        return "cannot resolve $parts"
    }
}
