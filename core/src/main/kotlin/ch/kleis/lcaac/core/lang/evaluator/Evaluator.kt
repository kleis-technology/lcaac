package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.protocol.Learner
import ch.kleis.lcaac.core.lang.evaluator.protocol.Oracle
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.SourceMap
import ch.kleis.lcaac.core.math.Operations
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import org.slf4j.LoggerFactory

/**
 * [recordDerivations]: record how each process is evaluated, see [EvaluationTrace.getDerivation].
 * This keeps the values of all variables and data source reads, and reads the rows of sums again.
 */
data class EvaluatorOptions(
    val recordDerivations: Boolean = false,
)

class Evaluator<Q, M> private constructor(
    private val symbolTable: SymbolTable<Q>,
    private val ops: Operations<Q, M>,
    private val sourceOps: DataSourceOperations<Q>,
    private val cache: ObjectKache<Pair<EProcessTemplate<Q>, EProductSpec<Q>>, EProcess<Q>>,
    private val options: EvaluatorOptions,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)
    private val oracle = Oracle(symbolTable, ops, sourceOps, cache, options.recordDerivations)

    constructor(
        symbolTable: SymbolTable<Q>,
        ops: Operations<Q, M>,
        sourceOps: DataSourceOperations<Q>,
        options: EvaluatorOptions = EvaluatorOptions(),
    ) : this(
        symbolTable,
        ops,
        sourceOps,
        InMemoryKache(maxSize = 1024) { strategy = KacheStrategy.LRU },
        options,
    )

    fun trace(initialRequests: Set<EProductSpec<Q>>): EvaluationTrace<Q> {
        val learner = Learner(initialRequests, ops)
        LOG.info("Start evaluation")
        try {
            var requests = learner.start()
            while (requests.isNotEmpty()) {
                val responses = requests.mapNotNull { request ->
                    within({ learner.requesterFrame(request) }) { oracle.answerRequest(request) }
                }.toSet()
                requests = learner.receive(responses)
            }
            LOG.info("End evaluation, found ${learner.trace.getNumberOfProcesses()} processes and ${learner.trace.getNumberOfSubstanceCharacterizations()} substances")
            return learner.trace
        } catch (e: Exception) {
            LOG.info("End evaluation with error $e")
            if (e is EvaluatorException) e.withSourceMap(symbolTable.sourceMap)
            throw e
        }
    }

    /** An evaluator that also knows [template], defined at the locations given by [sourceMap]. */
    fun with(template: EProcessTemplate<Q>, sourceMap: SourceMap = SourceMap.empty()): Evaluator<Q, M> {
        val processKey = ProcessKey(template.body.name)
        if (symbolTable.processTemplates[processKey] != null) throw IllegalStateException("Process ${template.body.name} already exists")
        val st = this.symbolTable.copy(
                processTemplates = this.symbolTable.processTemplates.plus(
                    mapOf(processKey to template)
                ),
                sourceMap = this.symbolTable.sourceMap + sourceMap,
            )
        return Evaluator(st, ops, sourceOps, cache, options)
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

