package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.protocol.Arena
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.FromProcess
import ch.kleis.lcaac.core.lang.expression.MatchLabels
import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.core.math.QuantityOperations
import org.slf4j.LoggerFactory

class Evaluator<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)

    // TODO: Accept a product spec instead
    fun trace(expression: EProcessTemplateApplication<Q>): EvaluationTrace<Q> {
        val requests = prepareRequests(expression)
        val arena = Arena(
            symbolTable,
            requests,
            ops,
        )
        return arena.run()
    }

    fun eval(expression: EProcessTemplateApplication<Q>): SystemValue<Q> {
        return trace(expression).getSystemValue()
    }

    private fun prepareRequests(expression: EProcessTemplateApplication<Q>): Set<EProductSpec<Q>> {
        val template = expression.template
        val body = template.body
        return body.products.map {
            it.product.copy(
                fromProcess = FromProcess(
                    body.name,
                    MatchLabels(body.labels),
                    template.params.plus(expression.arguments)
                )
            )
        }.toSet()
    }
}

