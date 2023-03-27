package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver

class Compiler(
    private val symbolTable: SymbolTable,
) {
    private val reduceAndComplete = ReduceAndComplete(symbolTable)
    private val processResolver = ProcessResolver(symbolTable)
    private val completeDefaultArguments = CompleteDefaultArguments(processResolver)
    private val everyInputProduct =
        TemplateExpression.eProcessFinal.expression.eProcess.inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product.eConstrainedProduct)


    fun compile(expression: TemplateExpression): UnlinkedSystem {
        return recursiveCompile(UnlinkedSystem.empty(), expression)
    }

    private fun recursiveCompile(
        accumulator: UnlinkedSystem,
        expression: TemplateExpression,
    ): UnlinkedSystem {
        // eval
        val e = completeDefaultArguments.apply(expression)
        val p = reduceAndComplete.apply(e)
        val v = p.toValue()

        // termination condition
        if (accumulator.containsProcess(v)) {
            return accumulator
        }

        // add evaluated process
        var result = accumulator.plus(v)

        // add substance characterizations
        val everySubstance =
            TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose
                    Every.list() compose
                    EBioExchange.substance.eSubstance
        everySubstance.getAll(p).forEach { substance ->
            symbolTable.getSubstanceCharacterization(substance.name)?.let {
                val scv = reduceAndComplete.apply(it).toValue()
                result = result.plus(scv)
            }
        }

        // recursively visit process template instances
        everyInputProduct.getAll(p)
            .forEach {
                resolveAndCheckCandidates(it)?.let { candidate ->
                    val template = candidate.second
                    val arguments = when (it.constraint) {
                        is FromProcessRef -> it.constraint.arguments
                        None -> emptyMap()
                    }
                    result = recursiveCompile(result, EInstance(template, arguments))
                }
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
