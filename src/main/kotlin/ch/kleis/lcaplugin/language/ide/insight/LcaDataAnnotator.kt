package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateWarnWithMessage
import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.isAssignementReciever
import ch.kleis.lcaplugin.language.ide.insight.LcaDataAnnotator.ResolveResult.*
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaDataAnnotator : Annotator {
    enum class ResolveResult {
        NORESOLVE, ONERESOLVE, MANYRESOLVE
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiDataRef && !isAssignementReciever(element)) {
            val name = element.name
            when (tryResolve(element)) {
                ONERESOLVE -> Unit

                NORESOLVE ->
                    annotateWarnWithMessage(element, holder, "Unresolved quantity reference $name")

                MANYRESOLVE ->
                    annotateWarnWithMessage(element, holder, "Quantity reference $name has several resolution targets")
            }
        }

    }

    private fun tryResolve(psiDataRef: PsiDataRef): ResolveResult {
        val fromPrelude = Prelude.unitMap<BasicNumber>()[psiDataRef.name]?.let { 1 } ?: 0
        val fromCode = psiDataRef.reference.multiResolve(false).size
        return when (fromPrelude + fromCode) {
            0 -> NORESOLVE
            1 -> ONERESOLVE
            else -> MANYRESOLVE
        }
    }

}
