package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.ProcessValue
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.SystemValue
import ch.kleis.lcaplugin.core.lang.expression.*

class RecursiveEvaluator(
    private val symbolTable: SymbolTable,
) {
    private val evaluator = Evaluator(symbolTable)

    fun eval(expression: TemplateExpression): SystemValue {
        val processes = aux(emptySet(), expression)
        return SystemValue(
            processes.toList(),
            emptyList(),
        )
    }

    private fun aux(visited: Set<ProcessValue>, expression: TemplateExpression): Set<ProcessValue> {
        val p = evaluator.step(expression)
        val v = evaluator.asValue(p)
        if (v !is ProcessValue) {
            throw EvaluatorException("$v is not a process")
        }
        if (visited.contains(v)) {
            return visited
        }

        val focus =
            TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct.constraint.fromProcessRef
        val constraints = focus.getAll(p)

        val newVisited = HashSet(visited)
        newVisited.add(v)

        constraints.forEach {
            val template = symbolTable.getTemplate(it.template.name) ?: throw EvaluatorException("unbounded template reference ${it.template.name}")
            val arguments = it.arguments
            newVisited.addAll(aux(newVisited, EInstance(template, arguments)))
        }

        return newVisited
    }
}

