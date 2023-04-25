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
            val result = recursiveCompile(SystemValue.empty(), expression)
            LOG.info("End recursive Compile, found ${result.processes.size} processes and ${result.substanceCharacterizations.size} substances")
            return result
        } catch (e: Exception) {
            LOG.info("End recursive Compile with error $e")
            throw e
        }
    }

    private fun recursiveCompile(
        accumulator: SystemValue,
        expression: TemplateExpression,
    ): SystemValue {
        // eval
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
            return accumulator
        }

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
        nextInstances.forEach {
            result = recursiveCompile(result, it)
        }
        return result
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

