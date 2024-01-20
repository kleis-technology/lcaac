package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.QuantityOperations

class LcaExpressionReducer<Q>(
    private val dataRegister: DataRegister<Q> = DataRegister.empty(),
    private val dataSourceRegister: DataSourceRegister<Q> = DataSourceRegister.empty(),
    private val ops: QuantityOperations<Q>,
    private val sourceOps: DataSourceOperations<Q>,
) {
    private val dataExpressionReducer = DataExpressionReducer(dataRegister, dataSourceRegister, ops, sourceOps)

    private fun push(data: Map<DataKey, DataExpression<Q>>): LcaExpressionReducer<Q> {
        TODO()
    }

    fun reduce(expression: LcaExpression<Q>): LcaExpression<Q> {
        return when (expression) {
            is EProcess -> reduceProcess(expression)
            is ESubstanceCharacterization -> reduceSubstanceCharacterization(expression)

            is EImpact -> reduceImpact(expression)
            is ETechnoExchange -> reduceTechnoExchange(expression)
            is EBioExchange -> reduceBioExchange(expression)

            is EIndicatorSpec -> reduceIndicatorSpec(expression)
            is ESubstanceSpec -> reduceSubstanceSpec(expression)
            is EProductSpec -> reduceProductSpec(expression)
        }
    }

    fun reduceSubstanceCharacterization(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        return ESubstanceCharacterization(
            reduceBioExchange(expression.referenceExchange),
            expression.impacts.flatMap { reduceBlock(it, this::reduceImpact) },
        )
    }


    private fun reduceProcess(expression: EProcess<Q>): EProcess<Q> {
        return EProcess(
            expression.name,
            expression.labels,
            expression.products.map(::reduceTechnoExchange),
            expression.inputs.flatMap { reduceBlock(it, this::reduceTechnoExchange) },
            expression.biosphere.flatMap { reduceBlock(it, this::reduceBioExchange) },
            expression.impacts.flatMap { reduceBlock(it, this::reduceImpact) },
        )
    }

    private fun reduceImpact(expression: EImpact<Q>) = EImpact(
        dataExpressionReducer.reduce(expression.quantity),
        reduceIndicatorSpec(expression.indicator),
    )

    private fun reduceBioExchange(expression: EBioExchange<Q>) = EBioExchange(
        dataExpressionReducer.reduce(expression.quantity),
        reduceSubstanceSpec(expression.substance),
    )

    private fun reduceTechnoExchange(expression: ETechnoExchange<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            dataExpressionReducer.reduce(expression.quantity),
            reduceProductSpec(expression.product),
            expression.allocation?.let { dataExpressionReducer.reduce(it) }
        )
    }

    // TODO: Test me
    private fun <E : LcaExpression<Q>> reduceBlock(expression: BlockExpression<E, Q>, onEntry: (E) -> E):
        List<EBlockEntry<E,
            Q>> {
        return when (expression) {
            is EBlockEntry -> listOf(
                EBlockEntry(onEntry(expression.entry))
            )

            is EBlockForEach -> {
                val ds = dataSourceRegister[DataSourceKey(expression.dataSourceRef)]
                    ?: throw EvaluatorException("unknown data source '${expression.dataSourceRef}'")
                sourceOps.readAll(ds)
                    .flatMap { record ->
                        val reducer = push(mapOf(
                            DataKey(expression.rowRef) to record
                        ))
                        expression.body
                            .flatMap { reducer.reduceBlock(it, onEntry) }
                    }.toList()
            }
        }
    }

    private fun reduceProductSpec(expression: EProductSpec<Q>): EProductSpec<Q> {
        return EProductSpec(
            expression.name,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
            expression.fromProcess?.let { ref ->
                ref.copy(
                    matchLabels = MatchLabels(ref.matchLabels.elements.mapValues { dataExpressionReducer.reduce(it.value) }),
                    arguments = ref.arguments.mapValues { dataExpressionReducer.reduce(it.value) }
                )
            },
        )
    }

    private fun reduceSubstanceSpec(expression: ESubstanceSpec<Q>): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            expression.name,
            expression.displayName,
            expression.type,
            expression.compartment,
            expression.subCompartment,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }

    private fun reduceIndicatorSpec(expression: EIndicatorSpec<Q>): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            expression.name,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }
}
