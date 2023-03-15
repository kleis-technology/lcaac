package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*

class RecursiveEvaluator(
    private val symbolTable: SymbolTable,
) {
    private val reduceAndComplete = ReduceAndComplete(symbolTable)
    private val processResolver = ProcessResolver(symbolTable)

    fun eval(expression: TemplateExpression): SystemValue {
        val state = State.empty()
        recursiveEval(state, expression)
        return state.asSystem()
    }

    private fun recursiveEval(
        state: State,
        expression: TemplateExpression,
    ) {
        // eval
        val p = reduceAndComplete.apply(expression)
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
        val everyConstrainedProduct =
            TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                    Every.list() compose
                    ETechnoExchange.product.eConstrainedProduct

        for (it in everyConstrainedProduct.getAll(p)) {
            val candidate = resolveAndCheckCandidates(it)
            val template = candidate?.second ?: continue
            val arguments = when (it.constraint) {
                is FromProcessRef -> it.constraint.arguments
                None -> emptyMap()
            }
            recursiveEval(
                state,
                EInstance(template, arguments),
            )
        }
    }

    private fun resolveAndCheckCandidates(product: EConstrainedProduct): Pair<String, TemplateExpression>? {
        val eProduct = product.product as EProduct
        return when (product.constraint) {
            is FromProcessRef -> {
                val processRef = product.constraint.template.name
                val candidates = processResolver.resolve(eProduct.name)
                if (candidates.size > 1) {
                    val candidateNames = candidates.map { it.first }
                    throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                }
                val candidate = candidates
                    .firstOrNull { it.first == processRef }
                    ?: throw EvaluatorException("no process '$processRef' providing '${eProduct.name}' found")
                return candidate
            }

            None -> {
                val candidates = processResolver.resolve(eProduct.name)
                if (candidates.size > 1) {
                    val candidateNames = candidates.map { it.first }
                    throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                }
                candidates.firstOrNull()
            }
        }
    }

    class State(
        private val processes: MutableSet<ProcessValue> = HashSet(),
        private val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue> = HashSet(),
    ) {
        constructor(state: State) : this(
            HashSet(state.processes),
            HashSet(state.substanceCharacterizations),
        )

        companion object {
            fun empty(): State {
                return State()
            }
        }

        fun containsProcess(process: ProcessValue) : Boolean {
            return processes.contains(process)
        }

        fun addProcess(process: ProcessValue) {
            processes.add(process)
        }

        fun addSubstanceCharacterization(substanceCharacterization: SubstanceCharacterizationValue) {
            substanceCharacterizations.add(substanceCharacterization)
        }

        fun addState(state: State) {
            processes.addAll(state.processes)
            substanceCharacterizations.addAll(state.substanceCharacterizations)
        }

        fun asSystem(): SystemValue {
            return SystemValue(
                processes = processes.toList(),
                substanceCharacterizations = substanceCharacterizations.toList(),
            )
        }
    }
}

