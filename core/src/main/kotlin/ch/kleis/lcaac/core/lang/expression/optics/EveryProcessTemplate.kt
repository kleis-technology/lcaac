package ch.kleis.lcaac.core.lang.expression.optics

import ch.kleis.lcaac.core.lang.expression.*

fun <Q> everyProcessTemplateInTemplateExpression() = Merge(
    listOf(
        ProcessTemplateExpression.eProcessTemplateApplication<Q>().template(),
        ProcessTemplateExpression.eProcessTemplate(),
    )
)