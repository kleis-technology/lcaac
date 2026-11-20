package ch.kleis.lcaac.server.api

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.BlockExpression
import ch.kleis.lcaac.core.lang.expression.EBlockEntry
import ch.kleis.lcaac.core.lang.expression.EBlockForEach
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EStringLiteral

/**
 * Checks an API definition against the model: every name it refers to must exist, and the labels of each
 * operation must select a process.
 */
class ApiChecker<Q>(private val symbolTable: SymbolTable<Q>) {
    private val templates = symbolTable.processTemplates.toMap().values.toList()
    private val processNames = templates.map { it.body.name }.toSet()
    private val productNames = templates.flatMap { t -> t.body.products.map { it.product.name } }.toSet()
    private val characterizations = symbolTable.substanceCharacterizations.toMap().values
    private val impactColumns: Set<String> = templates.flatMap { t ->
        entries(t.body.impacts).map { it.indicator.name } + entries(t.body.biosphere).map { it.substance.name }
    }.toSet() + characterizations.flatMap { c ->
        entries(c.impacts).map { it.indicator.name } + c.referenceExchange.substance.name
    }

    /**
     * The processes that an operation can evaluate: those named [Operation.process] with exactly the labels
     * fixed by the operation or given by its inputs, with the fixed values.
     */
    fun candidates(operation: Operation): List<EProcessTemplate<Q>> = templates.filter { t ->
        t.body.name == operation.process &&
            t.body.labels.keys == labelKeys(operation) &&
            operation.labels.all { (k, v) -> t.body.labels[k]?.value == v }
    }

    private fun labelKeys(operation: Operation): Set<String> =
        operation.labels.keys + operation.inputs.values.mapNotNull { it.label }

    /**
     * The values of the label [label] among the processes of [operation].
     */
    fun labelValues(operation: Operation, label: String): List<String> =
        candidates(operation).mapNotNull { it.body.labels[label]?.value }.distinct().sorted()

    fun check(located: LocatedApiDefinition): List<ApiDefinitionError> =
        located.definition.operations.flatMap { (name, operation) ->
            checkOperation(located, listOf("operations", name), operation)
        }

    private fun checkOperation(
        located: LocatedApiDefinition,
        path: List<String>,
        operation: Operation,
    ): List<ApiDefinitionError> {
        val errors = mutableListOf<ApiDefinitionError>()
        fun error(subPath: List<String>, message: String) = errors.add(located.error(path + subPath, message))

        if (operation.process !in processNames) {
            error(listOf("process"), "unknown process '${operation.process}'")
            return errors
        }
        val named = templates.filter { it.body.name == operation.process }
        val candidates = candidates(operation)

        operation.inputs.forEach { (name, input) ->
            val inputPath = listOf("inputs", name)
            when {
                (input.parameter == null) == (input.label == null) ->
                    error(inputPath, "an input maps to exactly one of 'parameter' or 'label'")

                input.parameter != null -> if (candidates.isNotEmpty() && candidates.any { input.parameter !in it.params }) {
                    error(inputPath + "parameter", "unknown parameter '${input.parameter}' of process '${operation.process}'")
                }

                input.label != null -> if (input.label in operation.labels) {
                    error(inputPath + "label", "label '${input.label}' is already fixed")
                } else if (named.none { input.label in it.body.labels }) {
                    error(inputPath + "label", "unknown label '${input.label}' of process '${operation.process}'")
                }
            }
        }
        if (candidates.isEmpty()) {
            val labels = operation.labels.map { (k, v) -> "$k=$v" } +
                operation.inputs.values.mapNotNull { it.label }.filter { it !in operation.labels }.map { "$it=<input>" }
            error(
                listOf("labels"),
                "no process '${operation.process}' has exactly the labels ${labels.joinToString(", ", "{", "}")}",
            )
        }

        operation.data.forEach { (name, data) ->
            if (name in operation.inputs) {
                error(listOf("data", name), "'$name' is already an input")
            }
            if (symbolTable.getDataSource(data.datasource) == null) {
                error(listOf("data", name, "datasource"), "unknown data source '${data.datasource}'")
            }
        }

        if (operation.outputs.isEmpty()) {
            error(emptyList(), "an operation has at least one output")
        }
        operation.outputs.forEach { (name, output) ->
            checkOutput(listOf("outputs", name), output, ::error)
        }
        return errors
    }

    private fun checkOutput(
        path: List<String>,
        output: Output,
        error: (List<String>, String) -> Unit,
    ) {
        val where = output.where
        val wherePath = path + "where"
        if (where.isEmpty()) {
            error(wherePath, "'where' needs at least one condition")
        }
        if (where.product != null && where.product !in productNames) {
            error(wherePath + "product", "unknown product '${where.product}'")
        }
        val processes = if (where.process == null) templates else {
            if (where.process !in processNames) {
                error(wherePath + "process", "unknown process '${where.process}'")
            }
            templates.filter { it.body.name == where.process }
        }
        where.labels.forEach { (label, condition) ->
            if (processes.none { label in it.body.labels }) {
                error(wherePath + "labels", "unknown label '$label'")
            }
            checkCondition(wherePath + listOf("labels", label), condition, error)
        }
        where.arguments.forEach { (argument, condition) ->
            if (processes.none { it.params[argument] is EStringLiteral<*> }) {
                error(wherePath + "arguments", "unknown string parameter '$argument'")
            }
            checkCondition(wherePath + listOf("arguments", argument), condition, error)
        }

        if (output.select.isEmpty()) {
            error(path + "select", "'select' needs at least one column")
        }
        output.select.forEach { column ->
            if (column !in Columns.fixed && column !in impactColumns) {
                error(path + "select", "unknown column '$column'")
            }
        }
        output.select.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            error(path + "select", "column '$it' is selected twice")
        }
        if (output.aggregate == Aggregate.SUM) {
            output.select.filter { it in Columns.identity }.forEach {
                error(path + "select", "column '$it' cannot be summed")
            }
            if (Columns.AMOUNT in output.select && where.product == null) {
                error(path + "select", "column '${Columns.AMOUNT}' is summed for one product only: set 'where.product'")
            }
        }
    }

    private fun checkCondition(path: List<String>, condition: Condition?, error: (List<String>, String) -> Unit) {
        if (condition != null && condition.values.isEmpty()) {
            error(path, "a condition needs at least one value, or null for an absent value")
        }
    }
}

private fun <E, Q> entries(blocks: List<BlockExpression<E, Q>>): List<E> = blocks.flatMap {
    when (it) {
        is EBlockEntry -> listOf(it.entry)
        is EBlockForEach -> entries(it.body)
    }
}
