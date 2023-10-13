package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.arena.Arena
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class Evaluator<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
) {
    fun trace(requests: Set<EProductSpec<Q>>): EvaluationTrace<Q> {
        val arena = Arena(
            symbolTable,
            requests,
            ops,
        )
        return arena.run()
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

