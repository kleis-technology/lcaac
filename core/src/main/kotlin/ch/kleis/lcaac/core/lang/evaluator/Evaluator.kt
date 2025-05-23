package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.protocol.Learner
import ch.kleis.lcaac.core.lang.evaluator.protocol.BareOracle
import ch.kleis.lcaac.core.lang.evaluator.protocol.CachedOracle
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.math.QuantityOperations
import org.slf4j.LoggerFactory

class Evaluator<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
    private val sourceOps: DataSourceOperations<Q>,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)
    private val oracle = CachedOracle(symbolTable, ops, sourceOps)

    fun trace(initialRequests: Set<EProductSpec<Q>>): EvaluationTrace<Q> {
        val learner = Learner(initialRequests, ops)
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

    fun with(template: EProcessTemplate<Q>): Evaluator<Q> {
        val processKey = ProcessKey(template.body.name)
        if (symbolTable.processTemplates[processKey] != null) throw IllegalStateException("Process ${template.body.name} already exists")
        val st = this.symbolTable.copy(
                processTemplates = this.symbolTable.processTemplates.plus(
                    mapOf(processKey to template)
                )
            )
        return Evaluator(st, ops, sourceOps)
    }

    fun trace(
        template: EProcessTemplate<Q>,
        arguments: Map<String, DataExpression<Q>> = emptyMap()
    ): EvaluationTrace<Q> {
        val requests = prepareRequests(template, arguments)
        return trace(requests)
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

