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

class Evaluator(
    private val symbolTable: SymbolTable,
) {
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
        return recursiveCompile(SystemValue.empty(), expression)
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
                val template = candidate.second as EProcessTemplate
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


    private fun resolveAndCheckCandidates(product: EConstrainedProduct): Pair<String, TemplateExpression>? {
        val eProduct =
            if (product.product is EProduct) product.product
            else throw EvaluatorException("unbound product ${product.product}")
        return when (product.constraint) {
            is FromProcessRef -> {
                val processRef = product.constraint.ref
                val candidates = processResolver.resolveByProductName(eProduct.name)
                if (candidates.size > 1) {
                    val candidateNames = candidates.map { it.first }
                    throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                }
                return candidates
                    .firstOrNull { it.first == processRef }
                    ?: throw EvaluatorException("no process '$processRef' providing '${eProduct.name}' found")
            }

            None -> {
                val candidates = processResolver.resolveByProductName(eProduct.name)
                if (candidates.size > 1) {
                    val candidateNames = candidates.map { it.first }
                    throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                }
                candidates.firstOrNull()
            }
        }
    }

}

