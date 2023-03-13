package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.ProcessValue
import ch.kleis.lcaplugin.core.lang.SubstanceCharacterizationValue
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.SystemValue
import ch.kleis.lcaplugin.core.lang.expression.*

class RecursiveEvaluator(
    private val symbolTable: SymbolTable,
) {
    private val evaluator = Evaluator(symbolTable)

    fun eval(expression: TemplateExpression): SystemValue {
        val state = aux(State.empty(), expression)
        return state.asSystem()
    }

    private fun aux(state: State, expression: TemplateExpression): State {
        // eval
        val p = evaluator.step(expression)
        val v = evaluator.asValue(p)
        if (v !is ProcessValue) {
            throw EvaluatorException("$v is not a process")
        }

        // termination condition
        if (state.processes.contains(v)) {
            return state
        }

        // add evaluated process
        val newState = State(state)
        newState.processes.add(v)

        // add substance characterizations
        val everySubstance =
            TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose
                    Every.list() compose
                    EBioExchange.substance.eSubstance
        everySubstance.getAll(p).forEach { substance ->
            symbolTable.getSubstanceCharacterization(substance.name)?.let {
                val scv = evaluator.eval(it)
                newState.substanceCharacterizations.add(scv)
            }
        }

        // recursively visit next process template instances
        val everyFromProcessRef =
            TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                    Every.list() compose
                    ETechnoExchange.product.eConstrainedProduct.constraint.fromProcessRef
        everyFromProcessRef.getAll(p).forEach {
            val template = symbolTable.getTemplate(it.template.name)
                ?: throw EvaluatorException("unbounded template reference ${it.template.name}")
            val arguments = it.arguments
            newState.add(aux(newState, EInstance(template, arguments)))
        }

        return newState
    }

    class State(
        val processes: MutableSet<ProcessValue> = HashSet(),
        val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue> = HashSet(),
    ) {
        constructor(state: State): this(HashSet(state.processes), HashSet(state.substanceCharacterizations))

        companion object {
            fun empty(): State {
                return State()
            }
        }

        fun add(state: State) {
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

