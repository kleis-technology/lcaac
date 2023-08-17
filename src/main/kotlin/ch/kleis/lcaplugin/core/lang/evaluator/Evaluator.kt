package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import arrow.optics.PEvery
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.step.CompleteDefaultArguments
import ch.kleis.lcaplugin.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaplugin.core.lang.evaluator.step.Reduce
import ch.kleis.lcaplugin.core.lang.evaluator.step.ReduceLabelSelectors
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver
import ch.kleis.lcaplugin.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import com.intellij.openapi.diagnostic.Logger

class Evaluator(
    symbolTable: SymbolTable,
) {
    companion object {
        private val LOG = Logger.getInstance(Evaluator::class.java)
    }

    private val reduceLabelSelectors = ReduceLabelSelectors(symbolTable)
    private val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
    private val reduce = Reduce(symbolTable)

    private val processResolver = ProcessResolver(symbolTable)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(symbolTable)
    private val dataReducer = DataExpressionReducer(symbolTable.data)
    private val everyInputProduct =
        ProcessTemplateExpression.eProcessFinal.expression.inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)
    private val everySubstance: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, ESubstanceSpec, ESubstanceSpec> =
        ProcessTemplateExpression.eProcessFinal.expression.biosphere
            .compose(Every.list())
            .compose(EBioExchange.substance)

    fun trace(expression: EProcessTemplateApplication): EvaluationTrace {
        LOG.info("Start recursive Compile")
        try {
            val result = EvaluationTrace.empty()
            recursiveCompile(result, HashSet(), HashSet(setOf(expression)))
            LOG.info("End recursive Compile, found ${result.getNumberOfProcesses()} processes and ${result.getNumberOfSubstanceCharacterizations()} substances")
            return result
        } catch (e: Exception) {
            LOG.info("End recursive Compile with error $e")
            throw e
        }
    }

    fun eval(expression: EProcessTemplateApplication): SystemValue {
        return trace(expression).getSystemValue()
    }

    private tailrec fun recursiveCompile(
        trace: EvaluationTrace,
        visited: HashSet<EProcessTemplateApplication>,
        batch: HashSet<EProcessTemplateApplication>,
    ) {
        // termination condition
        if (batch.isEmpty()) return

        // eval : breadth-first strategy
        val nextBatch = HashSet<EProcessTemplateApplication>()
        batch.forEach { expression ->
            if (visited.contains(expression)) LOG.warn("Should not be present in already processed expressions $expression")
            visited.add(expression)

            val reduced = expression
                .let(reduceLabelSelectors::apply)
                .let(completeDefaultArguments::apply)
                .let(reduce::apply)
                .let(CompleteTerminals::apply)

            val inputProductsModified = everyInputProduct.modify(reduced) { spec: EProductSpec ->
                processResolver.resolve(spec)?.let { template ->
                    val body = template.body
                    val labels = spec.fromProcess?.matchLabels
                        ?: MatchLabels(template.body.labels)
                    val arguments = spec.fromProcess?.arguments
                        ?: template.params.mapValues { entry -> dataReducer.reduce(entry.value) }
                    val unit = body.products.firstOrNull {
                        it.product.name == spec.name
                    }?.product?.referenceUnit?.let {
                        dataReducer.reduce(it)
                    } ?: spec.referenceUnit
                    nextBatch.add(EProcessTemplateApplication(template, arguments))
                    spec.copy(
                        referenceUnit = unit,
                        fromProcess =
                        FromProcess(
                            body.name,
                            labels,
                            arguments,
                        )
                    )
                } ?: spec
            }
            val substancesModified = everySubstance.modify(inputProductsModified) { spec ->
                resolveSubstanceCharacterizationBySubstanceSpec(spec)?.let {
                    val substanceCharacterization = it
                        .let(reduce::apply)
                        .let(CompleteTerminals::apply)
                    trace.add(substanceCharacterization.toValue())
                    substanceCharacterization.referenceExchange.substance
                } ?: spec
            }
            val v = substancesModified.toValue()

            if (trace.contains(v)) {
                LOG.warn("This expression should not be present in accumulator $expression and $v")
            } else {
                // accumulate evaluated process
                trace.add(v)
                nextBatch.removeIf { visited.contains(it) }
            }
        }

        // end stage
        trace.commit()

        // continue
        recursiveCompile(trace, visited, nextBatch)
    }

    private fun resolveSubstanceCharacterizationBySubstanceSpec(spec: ESubstanceSpec): ESubstanceCharacterization? {
        return substanceCharacterizationResolver.resolve(spec)?.takeIf { it.hasImpacts() }
    }
}

