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
    private val completeTerminals = CompleteTerminals()

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

    fun eval(expression: EProcessTemplateApplication): SystemValue {
        LOG.info("Start recursive Compile")
        try {
            val result = SystemValue.empty()
            recursiveCompile(result, HashSet(), HashSet(setOf(expression)))
            LOG.info("End recursive Compile, found ${result.processes.size} processes and ${result.substanceCharacterizations.size} substances")
            return result
        } catch (e: Exception) {
            LOG.info("End recursive Compile with error $e")
            throw e
        }
    }

    private tailrec fun recursiveCompile(
        accumulator: SystemValue,
        visited: HashSet<EProcessTemplateApplication>,
        toProcess: HashSet<EProcessTemplateApplication>,
    ) {
        // termination condition
        if (toProcess.isEmpty()) return

        // eval
        val expression = toProcess.first(); toProcess.remove(expression)
        if (visited.contains(expression)) LOG.warn("Should not be present in already processed expressions $expression")
        visited.add(expression)

        val reduced = expression
            .let(reduceLabelSelectors::apply)
            .let(completeDefaultArguments::apply)
            .let(reduce::apply)
            .let(completeTerminals::apply)

        val nextInstances = HashSet<EProcessTemplateApplication>()
        val inputProductsModified = everyInputProduct.modify(reduced) { spec: EProductSpec ->
            resolveProcessTemplateByProductSpec(spec)?.let { template ->
                val body = template.body
                val labels = spec.fromProcess?.matchLabels
                    ?: MatchLabels(template.body.labels)
                val arguments = spec.fromProcess?.arguments
                    ?: template.params.mapValues { entry -> dataReducer.reduce(entry.value) }
                nextInstances.add(EProcessTemplateApplication(template, arguments))
                spec.copy(
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
                accumulator.add(substanceCharacterization.toValue())
                substanceCharacterization.referenceExchange.substance
            } ?: spec
        }
        val v = substancesModified.toValue()

        if (accumulator.containsProcess(v)) {
            LOG.warn("This expression should not be present in accumulator $expression and $v")
            recursiveCompile(accumulator, visited, toProcess)
        } else {

            // add evaluated process
            accumulator.add(v)

            // recursively visit process template instances
            nextInstances.forEach { if (!visited.contains(it)) toProcess.add(it) }

            recursiveCompile(accumulator, visited, toProcess)
        }
    }

    private fun resolveSubstanceCharacterizationBySubstanceSpec(spec: ESubstanceSpec): ESubstanceCharacterization? {
        return substanceCharacterizationResolver.resolve(spec)?.takeIf { it.hasImpacts() }
    }

    private fun resolveProcessTemplateByProductSpec(spec: EProductSpec): EProcessTemplate? {
        return processResolver.resolve(spec)
    }
}

