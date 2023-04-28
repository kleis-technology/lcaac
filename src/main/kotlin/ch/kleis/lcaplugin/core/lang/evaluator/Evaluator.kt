package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import arrow.optics.PEvery
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.QuantityExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.step.CompleteDefaultArguments
import ch.kleis.lcaplugin.core.lang.evaluator.step.ReduceAndComplete
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import com.intellij.openapi.diagnostic.Logger

class Evaluator(
    private val symbolTable: SymbolTable,
) {
    companion object {
        private val LOG = Logger.getInstance(Evaluator::class.java)
    }

    private val reduceAndComplete = ReduceAndComplete(symbolTable)
    private val processResolver = ProcessResolver(symbolTable)
    private val quantityReducer = QuantityExpressionReducer(symbolTable.quantities, symbolTable.units)
    private val completeDefaultArguments = CompleteDefaultArguments(processResolver)
    private val everyInputProduct =
        ProcessTemplateExpression.eProcessFinal.expression.inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)
    private val everySubstance: PEvery<ProcessTemplateExpression, ProcessTemplateExpression, ESubstanceSpec, ESubstanceSpec> =
        ProcessTemplateExpression.eProcessFinal.expression.biosphere
            .compose(Every.list())
            .compose(EBioExchange.substance)

    fun eval(expression: ProcessTemplateExpression): SystemValue {
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
        visited: HashSet<ProcessTemplateExpression>,
        toProcess: HashSet<ProcessTemplateExpression>,
    ) {
        if (toProcess.isEmpty()) return
        // eval
        val expression = toProcess.first()
        toProcess.remove(expression)
        if (visited.contains(expression)) LOG.warn("Should not be present in already processed expressions $expression")
        visited.add(expression)

        val completed = completeDefaultArguments.apply(expression)
        val reduced = reduceAndComplete.apply(completed)
        val nextInstances = HashSet<EProcessTemplateApplication>()
        val e = everyInputProduct.modify(reduced) { spec ->
            maybeResolveProcessTemplateFromProduct(spec)?.let { candidate ->
                val template = candidate as EProcessTemplate
                val body = template.body
                val arguments = spec.fromProcessRef?.arguments
                    ?: template.params.mapValues { entry -> quantityReducer.reduce(entry.value) }
                nextInstances.add(EProcessTemplateApplication(template, arguments))
                spec.withFromProcessRef(
                    FromProcessRef(
                        body.name,
                        arguments,
                    )
                )
            } ?: spec
        }
        val v = e.toValue()

        // termination condition
        if (accumulator.containsProcess(v)) {
            LOG.warn("This expression should not be present in accumulator $expression and $v")
            recursiveCompile(accumulator, visited, toProcess)
        } else {

            // add evaluated process
            accumulator.plus(v)

            // add substance characterizations
            everySubstance.getAll(reduced).forEach { substance ->
                symbolTable.getSubstanceCharacterization(substance.name)?.let {
                    val scv = reduceAndComplete.apply(it).toValue()
                    accumulator.plus(scv)
                }
            }

            // recursively visit process template instances
            nextInstances.forEach { if (!visited.contains(it)) toProcess.add(it) }

            recursiveCompile(accumulator, visited, toProcess)
        }
    }

    private fun maybeResolveProcessTemplateFromProduct(spec: EProductSpec): ProcessTemplateExpression? {
        return spec.fromProcessRef?.ref?.let { processName ->
            val candidate = processResolver.resolveByProductName(spec.name)
            return candidate ?: throw EvaluatorException("no process '$processName' providing '${spec.name}' found")
        } ?: processResolver.resolveByProductName(spec.name)
    }

}

