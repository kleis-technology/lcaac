package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver
import ch.kleis.lcaplugin.core.lang.resolver.ProductResolver

class Compiler(
    private val symbolTable: SymbolTable,
) {
    private val reduceAndComplete = ReduceAndComplete(symbolTable)
    private val productResolver = ProductResolver(symbolTable)
    private val processResolver = ProcessResolver(symbolTable)
    private val completeDefaultArguments = CompleteDefaultArguments(processResolver)
    private val everyInputProduct =
        TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct


    fun compile(expression: TemplateExpression): UnlinkedSystem {
        val state = UnlinkedSystem.empty()
        recursiveCompile(state, expression)
        return state
    }

    private fun recursiveCompile(
        state: UnlinkedSystem,
        expression: TemplateExpression,
    ) {
        // eval
        val e = completeDefaultArguments.apply(expression)
        val p = reduceAndComplete.apply(e)
        val v = p.toValue()

        // termination condition
        if (state.containsProcess(v)) {
            return
        }

        // add evaluated process
        state.addProcess(v)

        // add substance characterizations
        val everySubstance =
            TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose
                    Every.list() compose
                    EBioExchange.substance.eSubstance
        everySubstance.getAll(p).forEach { substance ->
            symbolTable.getSubstanceCharacterization(substance.name)?.let {
                val scv = reduceAndComplete.apply(it).toValue()
                state.addSubstanceCharacterization(scv)
            }
        }

        // recursively visit process template instances
        everyInputProduct.getAll(p)
            .forEach {
                val candidate = resolveAndCheckCandidates(it) ?: return
                val template = candidate.second
                val arguments = when (it.constraint) {
                    is FromProcessRef -> it.constraint.arguments
                    None -> emptyMap()
                }
                recursiveCompile(state, EInstance(template, arguments))
            }
    }


    private fun resolveAndCheckCandidates(product: EConstrainedProduct): Pair<String, TemplateExpression>? {
        val eProduct = when (product.product) {
            is EProduct -> product.product
            is EProductRef -> productResolver.resolve(product.product.name)
        } ?: throw EvaluatorException("unbound product ${product.product}")
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
