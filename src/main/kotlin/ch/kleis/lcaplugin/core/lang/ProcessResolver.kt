package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyProcessTemplateInTemplateExpression

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(productName: String): Set<TemplateExpression> {
        val optics = Merge(
            listOf(
                everyProcessTemplateInTemplateExpression compose EProcessTemplate.body,
                TemplateExpression.eProcessFinal.expression,
            )
        ) compose
                LcaProcessExpression.eProcess.products compose
                Every.list() compose
                ETechnoExchange.product.lcaUnconstrainedProductExpression compose
                Merge(
                    listOf(
                        LcaUnconstrainedProductExpression.eProduct.name,
                        LcaUnconstrainedProductExpression.eProductRef.name,
                    )
                )
        return symbolTable.processTemplates.values
            .filter {
                optics.getAll(it).contains(productName)
            }
            .toSet()
    }
}
