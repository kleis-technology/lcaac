package com.github.albanseurat.lcaplugin.language.ide.insight

import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement


class LcaAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is LcaDatasetDefinition) {
            if (element.name?.length!! < 2) {

                holder.newAnnotation(HighlightSeverity.ERROR, "Dataset name must contains more than 1 characters")
                    .range(element.identifier)
                    .highlightType(ProblemHighlightType.GENERIC_ERROR)
                    .create();
            }
        }
    }
}