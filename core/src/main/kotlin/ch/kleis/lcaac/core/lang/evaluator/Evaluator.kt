package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.protocol.Oracle
import ch.kleis.lcaac.core.lang.evaluator.protocol.Learner
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
        val learner = Learner(initialRequests, ops)
        val oracle = Oracle(symbolTable, ops)
        LOG.info("Start evaluation")
        try {
            var requests = learner.start()
            while (requests.isNotEmpty()) {
                val responses = oracle.answer(requests)
                requests = learner.receive(responses)
            }
            LOG.info("End evaluation, found ${learner.trace.getNumberOfProcesses()} processes and ${learner.trace.getNumberOfSubstanceCharacterizations()} substances")
            return learner.trace
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

