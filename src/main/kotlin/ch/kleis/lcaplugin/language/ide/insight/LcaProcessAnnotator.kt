package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaProcessAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaProcess) {
            return
        }

        val products = element.getProducts()
        if (products.size <= 1) {
            return
        }

        val productNames = products.map { it.outputProductSpec.name }
        val productsWithoutAllocationFactors = products
            .filter { it.outputProductSpec.allocateField == null }
        if (productsWithoutAllocationFactors.isNotEmpty()) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "some products in $productNames are missing allocation factors",
            ).range(element.blockProductsList.first())
                .highlightType(ProblemHighlightType.ERROR)
                .create()
        }
    }
}
