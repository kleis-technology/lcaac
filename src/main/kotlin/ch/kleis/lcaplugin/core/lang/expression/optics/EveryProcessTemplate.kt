package ch.kleis.lcaplugin.core.lang.expression.optics

import ch.kleis.lcaplugin.core.lang.expression.*

val everyProcessTemplateInTemplateExpression = Merge(
    listOf(
        ProcessTemplateExpression.eProcessTemplate,
        ProcessTemplateExpression.eProcessTemplateApplication.template,
    )
)

val everyEProcessInProcessTemplateExpression = Merge(
    listOf(
        ProcessTemplateExpression.eProcessTemplate.body,
        ProcessTemplateExpression.eProcessTemplateApplication.template.body,
        ProcessTemplateExpression.eProcessFinal.expression,
    )
)


