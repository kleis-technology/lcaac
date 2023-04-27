package ch.kleis.lcaplugin.core.lang.fixture

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Index
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyProcessTemplateInTemplateExpression

class IndexFixture {
    companion object {
        fun indexTemplate(register: Register<ProcessTemplateExpression>): Index<ProcessTemplateExpression> {
            return Index(register, Merge(
                listOf(
                    everyProcessTemplateInTemplateExpression compose EProcessTemplate.body,
                    ProcessTemplateExpression.eProcessFinal.expression,
                )
            ) compose
                    LcaProcessExpression.eProcess.products compose
                    Every.list() compose
                    ETechnoExchange.product.eConstrainedProduct.product compose
                    Merge(
                        listOf(
                            LcaUnconstrainedProductExpression.eProduct.name,
                            LcaUnconstrainedProductExpression.eProductRef.name,
                        )
                    )
            )
        }
    }
}
