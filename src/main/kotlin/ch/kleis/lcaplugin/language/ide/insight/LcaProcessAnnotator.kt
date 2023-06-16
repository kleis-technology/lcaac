package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaplugin.psi.LcaProcess
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
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
            annotateErrWithMessage(
                element.blockProductsList.first(),
                holder,
                "some products in $productNames are missing allocation factors",
            )
        }
    }
}
