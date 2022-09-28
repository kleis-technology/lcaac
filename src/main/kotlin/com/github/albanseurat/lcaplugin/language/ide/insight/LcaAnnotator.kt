package com.github.albanseurat.lcaplugin.language.ide.insight

import com.github.albanseurat.lcaplugin.psi.LcaInputExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement


class LcaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if( element is LcaInputExchange && element.reference?.resolve() == null) {
            element.nameIdentifier?.let {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved reference : ${it.text}")
                    .range(it)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .withFix(CreateDatasetAction(it.text))
                    .create()
            }
        }
    }
}