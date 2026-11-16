package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.ExchangeFrame
import ch.kleis.lcaac.core.lang.evaluator.ForEachFrame
import ch.kleis.lcaac.core.lang.evaluator.within
import ch.kleis.lcaac.core.lang.evaluator.derivation.DataSourceRead
import ch.kleis.lcaac.core.lang.evaluator.derivation.DerivationRecorder
import ch.kleis.lcaac.core.lang.evaluator.derivation.ReadKind
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.source.BlockPath
import ch.kleis.lcaac.core.lang.source.Section
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.source.substanceKey
import ch.kleis.lcaac.core.math.QuantityOperations

class LcaExpressionReducer<Q>(
    private val dataRegister: DataRegister<Q> = DataRegister.empty(),
    private val dataSourceRegister: DataSourceRegister<Q> = DataSourceRegister.empty(),
    private val ops: QuantityOperations<Q>,
    private val sourceOps: DataSourceOperations<Q>,
    /** The process or substance being reduced, to locate errors in its exchanges. */
    private val owner: SourcePath.ExchangeOwner? = null,
    /** Where a name is defined, to locate errors in its definition. */
    private val pathOf: (String) -> SourcePath? = { null },
    /** Records where the exchanges come from and, if asked, how they were evaluated. */
    private val recorder: DerivationRecorder<Q>? = null,
) {
    private val dataExpressionReducer = DataExpressionReducer(dataRegister, dataSourceRegister, ops, sourceOps, pathOf, recorder)

    private fun push(
        data: Map<DataKey, DataExpression<Q>>,
        paths: (String) -> SourcePath? = { null },
    ): LcaExpressionReducer<Q> {
        val localRegister = DataRegister(dataRegister)
            .plus(data)
        return LcaExpressionReducer(
            localRegister, dataSourceRegister, ops, sourceOps, owner,
            pathOf = { paths(it) ?: pathOf(it) },
            recorder = recorder,
        )
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
        val reducer = LcaExpressionReducer(
            dataRegister, dataSourceRegister, ops, sourceOps,
            SourcePath.Substance(expression.substanceKey()), pathOf,
        )
        return ESubstanceCharacterization(
            reducer.reduceBioExchange(expression.referenceExchange),
            expression.impacts.flatMapIndexed { i, block ->
                reducer.reduceBlock(block, BlockPath.of(Section.IMPACTS, i), impactLabel) { reduceImpact(it) }
            },
        )
    }


    private fun reduceProcess(expression: EProcess<Q>): EProcess<Q> {
        return EProcess(
            expression.name,
            expression.labels,
            expression.products.mapIndexed { i, exchange ->
                val path = BlockPath.of(Section.PRODUCTS, i)
                within({ exchangeFrame(path, "product", exchange.product.name) }) {
                    recording(path) { reduceTechnoExchange(exchange) }
                }
            },
            expression.inputs.flatMapIndexed { i, block ->
                reduceBlock(block, BlockPath.of(Section.INPUTS, i), inputLabel) { reduceTechnoExchange(it) }
            },
            expression.biosphere.flatMapIndexed { i, block ->
                reduceBlock(block, BlockPath.of(Section.BIOSPHERE, i), bioLabel) { reduceBioExchange(it) }
            },
            expression.impacts.flatMapIndexed { i, block ->
                reduceBlock(block, BlockPath.of(Section.IMPACTS, i), impactLabel) { reduceImpact(it) }
            },
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

    /*
        Blocks
     */

    /** Kind and name of an exchange, to describe it in errors. */
    private val inputLabel: (ETechnoExchange<Q>) -> Pair<String, String> = { "input" to it.product.name }
    private val bioLabel: (EBioExchange<Q>) -> Pair<String, String> =
        { (it.substance.type?.value?.lowercase() ?: "biosphere exchange") to it.substance.name }
    private val impactLabel: (EImpact<Q>) -> Pair<String, String> = { "impact" to it.indicator.name }

    /**
     * Reduces an exchange, or expands a for_each block into the exchanges of each row.
     * [path] is where the block is written in the process, or null outside of a process.
     */
    private fun <E> reduceBlock(
        block: BlockExpression<E, Q>,
        path: BlockPath?,
        label: (E) -> Pair<String, String>,
        reduceEntry: LcaExpressionReducer<Q>.(E) -> E,
    ): List<EBlockEntry<E, Q>> {
        return when (block) {
            is EBlockEntry -> within({ label(block.entry).let { (kind, name) -> exchangeFrame(path, kind, name) } }) {
                listOf(EBlockEntry(recording(path) { reduceEntry(block.entry) }))
            }

            is EBlockForEach -> within({ forEachFrame(path, block.rowRef, null) }) {
                val ds = dataExpressionReducer.evalDataSource(block.dataSource)
                val records = sourceOps.getAll(ds)
                    .map { dataExpressionReducer.reduce(it) }
                    .toList()
                val forEach = exchangePath(path)
                if (recorder != null && forEach != null) {
                    recorder.forEach(forEach, block.rowRef, DataSourceRead(ReadKind.FOR_EACH, ds, records.filterIsInstance<ERecord<Q>>()))
                }
                records.withIndex().flatMap { (row, record) ->
                    within({ forEachFrame(path, block.rowRef, row) }) {
                        val reducer = push(mapOf(
                            DataKey(block.rowRef) to record
                        )).push(
                            block.locals.mapKeys { DataKey(it.key) },
                            localPaths(block.locals.keys, path),
                        )
                        val reduceRow = {
                            block.body.flatMapIndexed { i, child ->
                                reducer.reduceBlock(child, path?.child(i), label, reduceEntry)
                            }
                        }
                        if (recorder != null && forEach != null) recorder.row(forEach, row, reduceRow) else reduceRow()
                    }
                }
            }
        }
    }

    /** Reduces the exchange written at [path], recording its origin. */
    private inline fun <T> recording(path: BlockPath?, crossinline reduce: () -> T): T {
        val exchange = exchangePath(path)
        return if (recorder != null && exchange != null) recorder.exchange(exchange) { reduce() } else reduce()
    }

    private fun exchangeFrame(path: BlockPath?, kind: String, name: String): ExchangeFrame? =
        exchangePath(path)?.let { ExchangeFrame(it, kind, name) }

    private fun forEachFrame(path: BlockPath?, rowRef: String, row: Int?): ForEachFrame? =
        exchangePath(path)?.let { ForEachFrame(it, rowRef, row) }

    private fun exchangePath(path: BlockPath?): SourcePath.Exchange? =
        if (owner != null && path != null) SourcePath.Exchange(owner, path) else null

    private fun localPaths(names: Set<String>, path: BlockPath?): (String) -> SourcePath? = { name ->
        if (owner != null && name in names) SourcePath.Variable(owner, name, path) else null
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
