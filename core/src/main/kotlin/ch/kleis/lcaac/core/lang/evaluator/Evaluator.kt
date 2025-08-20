package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.protocol.Oracle
import ch.kleis.lcaac.core.lang.evaluator.protocol.Learner
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.math.Operations
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import org.slf4j.LoggerFactory

class Evaluator<Q, M>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: Operations<Q, M>,
    private val sourceOps: DataSourceOperations<Q>,
    private val cache: ObjectKache<Pair<EProcessTemplate<Q>, EProductSpec<Q>>, EProcess<Q>> = InMemoryKache(
        maxSize = 1024
    ) {
        strategy = KacheStrategy.LRU
    }
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)
    private val oracle = Oracle(symbolTable, ops, sourceOps, cache)

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

    fun with(template: EProcessTemplate<Q>): Evaluator<Q, M> {
        val processKey = ProcessKey(template.body.name)
        if (symbolTable.processTemplates[processKey] != null) throw IllegalStateException("Process ${template.body.name} already exists")
        val st = this.symbolTable.copy(
                processTemplates = this.symbolTable.processTemplates.plus(
                    mapOf(processKey to template)
                )
            )
        return Evaluator(st, ops, sourceOps, cache) // TODO: since we modify the symbol table, is it ok?
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

