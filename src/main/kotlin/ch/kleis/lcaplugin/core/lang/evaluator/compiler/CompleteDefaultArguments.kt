package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver

class CompleteDefaultArguments(
    private val processResolver: ProcessResolver
) {
    private val everyInputProduct = TemplateExpression.eProcessTemplate.body.eProcess.inputs compose
            Every.list() compose
            ETechnoExchange.product.eConstrainedProduct

    fun apply(expression: TemplateExpression): TemplateExpression {
        return everyInputProduct.modify(expression) {
            when (it.constraint) {
                is FromProcessRef -> {
                    val process = processResolver.resolve(it.constraint.ref)
                        ?: throw EvaluatorException("unknown process ${it.constraint.ref}")
                    if (process !is EProcessTemplate) {
                        throw EvaluatorException("${it.constraint.ref} cannot be invoked")
                    }
                    val actualArguments = process.params.plus(it.constraint.arguments)
                    EConstrainedProduct(
                        it.product,
                        FromProcessRef(
                            it.constraint.ref,
                            actualArguments,
                        )
                    )
                }

                None -> it
            }
        }
    }
}
