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
        TemplateExpression.eProcessFinal.expression.eProcess.inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product.eConstrainedProduct)
    private val everySubstance: PEvery<TemplateExpression, TemplateExpression, ESubstance, ESubstance> =
        TemplateExpression.eProcessFinal.expression.eProcess.biosphere
            .compose(Every.list())
            .compose(EBioExchange.substance.eSubstance)

    fun eval(expression: TemplateExpression): SystemValue {
        LOG.info("Start recursive Compile")
        LOG.info("End recursive Compile")
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
        visited: HashSet<TemplateExpression>,
        toProcess: HashSet<TemplateExpression>,
    ) {
        if (toProcess.isEmpty()) return
        // eval
        val expression = toProcess.first()
        toProcess.remove(expression)
        if (visited.contains(expression)) LOG.warn("Should not be present in already processed expressions $expression")
        visited.add(expression)

        val completed = completeDefaultArguments.apply(expression)
        val reduced = reduceAndComplete.apply(completed)
        val nextInstances = HashSet<EInstance>()
        val e = everyInputProduct.modify(reduced) {
            resolveAndCheckCandidates(it)?.let { candidate ->
                val template = candidate as EProcessTemplate
                val body = template.body as EProcess
                val arguments = when (it.constraint) {
                    is FromProcessRef -> it.constraint.arguments
                    None -> template.params.mapValues { entry -> quantityReducer.reduce(entry.value) }
                }
                nextInstances.add(EInstance(template, arguments))
                EConstrainedProduct(
                    it.product,
                    FromProcessRef(
                        body.name,
                        arguments,
                    )
                )
            } ?: it
        }
        val v = e.toValue()

        // termination condition
        if (accumulator.containsProcess(v)) {
            LOG.warn("This expression should not be present in accumulator $expression and $v")
            recursiveCompile(accumulator, visited, toProcess)
        } else {

            // add evaluated process
            var result = accumulator.plus(v)

            // add substance characterizations
            val everySubstance =
                everySubstance
            everySubstance.getAll(reduced).forEach { substance ->
                symbolTable.getSubstanceCharacterization(substance.name)?.let {
                    val scv = reduceAndComplete.apply(it).toValue()
                    result = result.plus(scv)
                }
            }

            // recursively visit process template instances
            nextInstances.forEach { if (!visited.contains(it)) toProcess.add(it) }

            recursiveCompile(result, visited, toProcess)
        }
    }


    private fun resolveAndCheckCandidates(product: EConstrainedProduct): TemplateExpression? {
        val eProduct =
            if (product.product is EProduct) product.product
            else throw EvaluatorException("unbound product ${product.product}")
        return when (product.constraint) {
            is FromProcessRef -> {
                val processRef = product.constraint.ref
                val candidates = processResolver.resolveByProductName(eProduct.name)
                return candidates
                    ?: throw EvaluatorException("no process '$processRef' providing '${eProduct.name}' found")
            }

            None -> processResolver.resolveByProductName(eProduct.name)
        }
    }

}

