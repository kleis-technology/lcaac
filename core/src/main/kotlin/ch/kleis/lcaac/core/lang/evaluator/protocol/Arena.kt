package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.math.QuantityOperations
import org.slf4j.LoggerFactory

class Arena<Q>(
    private val proponent: Proponent<Q>,
    private val opponent: Opponent<Q>,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Arena::class.java)

    constructor(
        symbolTable: SymbolTable<Q>,
        requests: Set<EProductSpec<Q>>,
        ops: QuantityOperations<Q>,
    ): this(
        Proponent(
            requests,
            ops,
        ),
        Opponent(symbolTable, ops)
    )

    fun run(): EvaluationTrace<Q> {
        LOG.info("Start evaluation")
        try {
            var requests = proponent.start()
            while (requests.isNotEmpty()) {
                val responses = opponent.receive(requests)
                requests = proponent.receive(responses)
            }
            LOG.info("End evaluation, found ${proponent.trace.getNumberOfProcesses()} processes and ${proponent.trace.getNumberOfSubstanceCharacterizations()} substances")
            return proponent.trace
        } catch (e: Exception) {
            LOG.info("End evaluation with error $e")
            throw e
        }
    }
}
