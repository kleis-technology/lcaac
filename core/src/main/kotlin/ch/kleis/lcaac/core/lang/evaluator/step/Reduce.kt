package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ExchangeFrame
import ch.kleis.lcaac.core.lang.evaluator.Helper
import ch.kleis.lcaac.core.lang.evaluator.ProcessFrame
import ch.kleis.lcaac.core.lang.evaluator.SubstanceFrame
import ch.kleis.lcaac.core.lang.evaluator.derivation.DerivationRecorder
import ch.kleis.lcaac.core.lang.evaluator.derivation.ExchangeOrigin
import ch.kleis.lcaac.core.lang.evaluator.derivation.ProcessOrigins
import ch.kleis.lcaac.core.lang.evaluator.kindOf
import ch.kleis.lcaac.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaac.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaac.core.lang.evaluator.within
import ch.kleis.lcaac.core.lang.expression.EBioBlockEntry
import ch.kleis.lcaac.core.lang.expression.EImpactBlockEntry
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ETechnoBlockEntry
import ch.kleis.lcaac.core.lang.expression.LcaExpression
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.source.processKey
import ch.kleis.lcaac.core.lang.source.substanceKey
import ch.kleis.lcaac.core.math.QuantityOperations

class Reduce<Q>(
    symbolTable: SymbolTable<Q>,
    ops: QuantityOperations<Q>,
    sourceOps: DataSourceOperations<Q>,
) {
    private val lcaReducer = LcaExpressionReducer(
        symbolTable.data,
        symbolTable.dataSources,
        ops,
        sourceOps,
        // Substances only see globals
        pathOf = { name -> if (symbolTable.data[DataKey(name)] != null) SourcePath.Global(name) else null },
    )
    private val templateReducer = TemplateExpressionReducer(
        ops,
        sourceOps,
        symbolTable.data,
        symbolTable.dataSources,
    )

    fun apply(expression: EProcessTemplateApplication<Q>, recorder: DerivationRecorder<Q>? = null): EProcess<Q> =
        within({ ProcessFrame(SourcePath.Process(expression.template.body.processKey())) }) {
            val reduced = templateReducer.reduce(expression, recorder)
            val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
            if (unboundedReferences.isNotEmpty()) {
                val error = EvaluatorException("unbounded references: $unboundedReferences")
                recorder?.let { exchangeUsingUnboundedReference(reduced, it.origins())?.let(error::within) }
                throw error
            }
            reduced
        }

    /** The first exchange of [process] that uses an unbounded reference, located with [origins]. */
    private fun exchangeUsingUnboundedReference(process: EProcess<Q>, origins: ProcessOrigins): ExchangeFrame? {
        val helper = Helper<Q>()
        fun frame(origin: ExchangeOrigin?, exchange: LcaExpression<Q>, kind: String, name: String) =
            origin?.takeIf { helper.allRequiredRefs(exchange).isNotEmpty() }?.let { ExchangeFrame(it.path, kind, name) }

        return process.products.withIndex().firstNotNullOfOrNull { (i, it) ->
            frame(origins.products.getOrNull(i), it, "product", it.product.name)
        } ?: process.inputs.withIndex().firstNotNullOfOrNull { (i, block) ->
            (block as? ETechnoBlockEntry<Q>)?.entry?.let { frame(origins.inputs.getOrNull(i), it, "input", it.product.name) }
        } ?: process.biosphere.withIndex().firstNotNullOfOrNull { (i, block) ->
            (block as? EBioBlockEntry<Q>)?.entry?.let { frame(origins.biosphere.getOrNull(i), it, kindOf(it.substance), it.substance.name) }
        } ?: process.impacts.withIndex().firstNotNullOfOrNull { (i, block) ->
            (block as? EImpactBlockEntry<Q>)?.entry?.let { frame(origins.impacts.getOrNull(i), it, "impact", it.indicator.name) }
        }
    }

    fun apply(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> =
        within({ SubstanceFrame(SourcePath.Substance(expression.substanceKey())) }) {
            val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
            val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
            if (unboundedReferences.isNotEmpty()) {
                throw EvaluatorException("unbounded references: $unboundedReferences")
            }
            reduced
        }


}
