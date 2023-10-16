package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.arena.Opponent
import ch.kleis.lcaac.core.lang.evaluator.arena.Proponent
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations
import org.slf4j.LoggerFactory

class Evaluator<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)

    fun trace(initialRequests: Set<EProductSpec<Q>>): EvaluationTrace<Q> {
        val proponent = Proponent(initialRequests, ops)
        val opponent = Opponent(symbolTable, ops)
        LOG.info("Start evaluation")
        try {
            var requests = proponent.start()
            while (requests.isNotEmpty()) {
                val responses = opponent.answer(requests)
                requests = proponent.receive(responses)
            }
            LOG.info("End evaluation, found ${proponent.trace.getNumberOfProcesses()} processes and ${proponent.trace.getNumberOfSubstanceCharacterizations()} substances")
            return proponent.trace
        } catch (e: Exception) {
            LOG.info("End evaluation with error $e")
            throw e
        }
    }

    fun trace(template: EProcessTemplate<Q>, arguments: Map<String, DataExpression<Q>> = emptyMap()): EvaluationTrace<Q> {
        return prepareRequests(template, arguments)
            .let(this::trace)
    }

    private fun <Q> prepareRequests(
        template: EProcessTemplate<Q>,
        arguments: Map<String, DataExpression<Q>> = emptyMap(),
    ): Set<EProductSpec<Q>> {
        val body = template.body
        return body.products.map {
            it.product.copy(
                fromProcess = FromProcess(
                    body.name,
                    MatchLabels(body.labels),
                    template.params.plus(arguments)
                )
            )
        }.toSet()
    }
}

