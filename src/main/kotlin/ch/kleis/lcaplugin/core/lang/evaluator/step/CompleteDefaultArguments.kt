package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver

class CompleteDefaultArguments(
    private val processResolver: ProcessResolver
) {
    private val everyInputProduct = ProcessTemplateExpression.eProcessTemplate.body.inputs compose
            Every.list() compose
            ETechnoExchange.product

    fun apply(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        return everyInputProduct.modify(expression) {
            it.fromProcessRef?.let { ref ->
                val process = processResolver.resolve(ref.ref)
                    ?: throw EvaluatorException("unknown process ${ref.ref}")
                val actualArguments = process.params.plus(ref.arguments)
                EProductSpec(
                    it.name,
                    it.referenceUnit,
                    FromProcessRef(
                        ref.ref,
                        actualArguments,
                    )
                )
            } ?: it
        }
    }
}
