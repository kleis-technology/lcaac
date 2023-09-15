package ch.kleis.lcaac.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteDefaultArguments
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.evaluator.step.ReduceLabelSelectors
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.ProcessResolver
import ch.kleis.lcaac.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.core.math.QuantityOperations

class Evaluator<Q>(
    symbolTable: SymbolTable<Q>,
    ops: QuantityOperations<Q>,
) {
    private val reduceLabelSelectors = ReduceLabelSelectors(symbolTable, ops)
    private val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
    private val reduce = Reduce(symbolTable, ops)
    private val completeTerminals = CompleteTerminals(ops)

    private val processResolver = ProcessResolver(symbolTable)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(symbolTable)
    private val dataReducer = DataExpressionReducer(symbolTable.data, ops)
    private val everyInputProduct =
        ProcessTemplateExpression.eProcessFinal<Q>().expression().inputs()
            .compose(Every.list())
            .compose(ETechnoExchange.product())
    private val everySubstance =
        ProcessTemplateExpression.eProcessFinal<Q>().expression().biosphere()
            .compose(Every.list())
            .compose(EBioExchange.substance())
    private val mapper = ToValue(ops)

    fun trace(expression: EProcessTemplateApplication<Q>): EvaluationTrace<Q> {
//        LOG.info("Start recursive Compile")
        try {
            val result = EvaluationTrace.empty<Q>()
            recursiveCompile(result, HashSet(), HashSet(setOf(expression)))
//            LOG.info("End recursive Compile, found ${result.getNumberOfProcesses()} processes and ${result.getNumberOfSubstanceCharacterizations()} substances")
            return result
        } catch (e: Exception) {
//            LOG.info("End recursive Compile with error $e")
            throw e
        }
    }

    fun eval(expression: EProcessTemplateApplication<Q>): SystemValue<Q> {
        return trace(expression).getSystemValue()
    }

    private tailrec fun recursiveCompile(
        trace: EvaluationTrace<Q>,
        visited: HashSet<EProcessTemplateApplication<Q>>,
        batch: HashSet<EProcessTemplateApplication<Q>>,
    ) {
        // termination condition
        if (batch.isEmpty()) return

        // eval : breadth-first strategy
        val nextBatch = HashSet<EProcessTemplateApplication<Q>>()
        batch.forEach loop@{ expression ->
            if (visited.contains(expression)) {
//                LOG.warn("Should not be present in already processed expressions $expression")
                return@loop
            }
            visited.add(expression)

            val reduced = expression
                .let(reduceLabelSelectors::apply)
                .let(completeDefaultArguments::apply)
                .let(reduce::apply)
                .let(completeTerminals::apply)

            val inputProductsModified = everyInputProduct.modify(reduced) { spec: EProductSpec<Q> ->
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
                        .let(completeTerminals::apply)
                    trace.add(with(mapper) { substanceCharacterization.toValue() })
                    substanceCharacterization.referenceExchange.substance
                } ?: spec
            }
            val v = with(mapper) { substancesModified.toValue() }

            if (trace.contains(v)) {
//                LOG.warn("This expression should not be present in accumulator $expression and $v")
            } else {
                // accumulate evaluated process
                trace.add(v)
            }
            nextBatch.removeIf { visited.contains(it) }
        }

        // end stage
        trace.commit()

        // continue
        recursiveCompile(trace, visited, nextBatch)
    }

    private fun resolveSubstanceCharacterizationBySubstanceSpec(spec: ESubstanceSpec<Q>): ESubstanceCharacterization<Q>? {
        return substanceCharacterizationResolver.resolve(spec)?.takeIf { it.hasImpacts() }
    }
}

