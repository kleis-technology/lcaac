package com.github.albanseurat.lcaplugin.language.reference

import com.github.albanseurat.lcaplugin.language.SearchTrait
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*


class SubstanceReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement>(element, textRange),
    SearchTrait, PsiPolyVariantReference {

    private val datasetIdentifier: String

    init {
        datasetIdentifier = element.text.substring(textRange.startOffset, textRange.endOffset)
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return this.findSubstances(element.project, datasetIdentifier)
            .map { PsiElementResolveResult(it) }.toTypedArray()
    }

}
