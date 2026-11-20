package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.assessment.TraceRow
import ch.kleis.lcaac.core.assessment.TraceTableProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.ArgumentFrame
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.expression.QuantityExpression
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.parseOverrides
import ch.kleis.lcaac.grammar.smartParseQuantityWithDefaultUnit
import ch.kleis.lcaac.server.api.Columns
import ch.kleis.lcaac.server.api.Operation
import ch.kleis.lcaac.server.api.RowSelector
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ErrorFrame(val description: String, val location: String? = null)

@Serializable
data class ErrorBody(
    val error: String,
    val frames: List<ErrorFrame> = emptyList(),
    val related: List<ErrorFrame> = emptyList(),
)

sealed interface OperationResult {
    data class Success(val body: JsonObject) : OperationResult

    /** [status] is 400 when the request cannot be read, 422 when the evaluation fails. */
    data class Failure(val status: Int, val body: ErrorBody) : OperationResult
}

private class RequestException(val body: ErrorBody) : Exception(body.error)

/**
 * Runs the operations of an API on a model: maps a request onto the process, evaluates it, and returns
 * the selected cells of its trace table.
 */
class OperationRunner(
    private val symbolTable: SymbolTable<BasicNumber>,
    private val config: LcaacConfig,
    private val connectors: Map<String, DataSourceConnector<BasicNumber>>,
    private val showLocations: Boolean = true,
) {
    private val ops = BasicOperations

    /**
     * What the requests of a batch share: the reads of the data sources, and the evaluator, hence the
     * processes evaluated once with `@cached`.
     */
    private inner class Session {
        val sourceOps = DefaultDataSourceOperations(ops, config, connectors)
        val reducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
        val evaluator by lazy { Evaluator(symbolTable, ops, sourceOps) }
    }

    fun run(operation: Operation, request: JsonObject): OperationResult = run(operation, request, Session())

    /**
     * Runs the requests one after the other, with the same evaluator. One failure fails the whole batch.
     */
    fun runBatch(operation: Operation, requests: List<JsonObject>): OperationResult {
        val session = Session()
        val results = requests.mapIndexed { index, request ->
            when (val result = run(operation, request, session)) {
                is OperationResult.Success -> result.body
                is OperationResult.Failure -> return OperationResult.Failure(
                    result.status,
                    result.body.copy(frames = result.body.frames + ErrorFrame("request 'requests[$index]'")),
                )
            }
        }
        return OperationResult.Success(buildJsonObject { put("results", JsonArray(results)) })
    }

    private fun run(operation: Operation, request: JsonObject, session: Session): OperationResult {
        val prepared = try {
            prepare(operation, request, session.reducer)
        } catch (e: RequestException) {
            return OperationResult.Failure(400, e.body)
        } catch (e: EvaluatorException) {
            return OperationResult.Failure(400, errorBody(e))
        }

        val rows = try {
            // Rows given by the request replace a data source, which a cached process may read:
            // the evaluator of the session cannot be used
            val evaluator = if (prepared.rows.isEmpty()) session.evaluator else Evaluator(
                prepared.rows.keys.fold(symbolTable) { st, name ->
                    st.overrideDatasourceConnector(DataSourceKey(name), InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME)
                },
                ops,
                session.sourceOps.overrideWith(
                    InMemoryConnector(
                        InMemoryConnectorKeys.defaultConfig(cacheEnabled = false),
                        prepared.rows.mapValues { InMemoryDatasource(it.value) },
                    )
                ),
            )
            val trace = evaluator.trace(prepared.template, prepared.arguments)
            TraceTableProgram(trace).run()
        } catch (e: EvaluatorException) {
            return OperationResult.Failure(422, errorBody(e))
        } catch (e: IllegalStateException) {
            return OperationResult.Failure(422, ErrorBody(e.message ?: e.toString()))
        } catch (e: IllegalArgumentException) {
            return OperationResult.Failure(422, ErrorBody(e.message ?: e.toString()))
        }

        val referenceRows = rows.takeWhile { it.demandedProduct == rows.first().demandedProduct }
        return OperationResult.Success(buildJsonObject {
            operation.outputs.forEach { (name, output) ->
                put(name, JsonArray(referenceRows.filter { it.matches(output.where) }.map { it.cells(output.select) }))
            }
        })
    }

    private class Prepared(
        val template: EProcessTemplate<BasicNumber>,
        val arguments: Map<String, DataExpression<BasicNumber>>,
        val rows: Map<String, List<RecordValue<BasicNumber>>>,
    )

    private fun prepare(
        operation: Operation,
        request: JsonObject,
        reducer: DataExpressionReducer<BasicNumber>,
    ): Prepared {
        request.keys.firstOrNull { it !in operation.inputs && it !in operation.data }?.let {
            throw RequestException(ErrorBody("unknown field '$it'"))
        }

        val labels = operation.labels + operation.inputs
            .filterValues { it.label != null }
            .mapNotNull { (name, input) -> request[name]?.let { input.label!! to stringOf(name, it) } }
        val template = symbolTable.getTemplate(operation.process, labels)
            ?: throw RequestException(ErrorBody("no process '${operation.process}' with labels $labels"))

        // Parsed under the names of the request, so that errors name the fields of the request
        val parameterInputs = operation.inputs.filterValues { it.parameter != null && it.parameter in template.params }
        val given = parameterInputs.keys.mapNotNull { name -> request[name]?.let { name to it } }.toMap()
        given.forEach { (name, value) ->
            if (template.params[parameterInputs[name]!!.parameter] is EStringLiteral<*>) stringOf(name, value)
        }
        val arguments = parseOverrides(
            reducer,
            given.keys.associateWith { template.params[parameterInputs[it]!!.parameter]!! },
            given.mapValues { textOf(it.key, it.value) },
        )
        arguments.forEach { (name, value) ->
            checkDimension(name, textOf(name, given[name]!!), reducer, value, template.params[parameterInputs[name]!!.parameter]!!)
        }

        val rows = operation.data.mapNotNull { (name, data) ->
            request[name]?.let { data.datasource to records(name, data.datasource, it, reducer) }
        }.toMap()
        return Prepared(template, arguments.mapKeys { parameterInputs[it.key]!!.parameter!! }, rows)
    }

    private fun records(
        field: String,
        dataSourceName: String,
        json: JsonElement,
        reducer: DataExpressionReducer<BasicNumber>,
    ): List<RecordValue<BasicNumber>> {
        val array = json as? JsonArray ?: throw RequestException(ErrorBody("field '$field': expected a list of rows"))
        val schema = reducer.evalDataSource(symbolTable.getDataSource(dataSourceName)!!).schema
        return array.mapIndexed { index, element ->
            val rowName = "$field[$index]"
            val row = element as? JsonObject ?: throw RequestException(ErrorBody("field '$rowName': expected a row"))
            row.keys.firstOrNull { it !in schema }?.let {
                throw RequestException(ErrorBody("field '$rowName': unknown column '$it'"))
            }
            RecordValue(schema.mapValues { (column, default) ->
                val name = "$rowName.$column"
                val value = row[column] ?: return@mapValues default
                when (default) {
                    is QuantityValue -> {
                        val text = textOf(name, value)
                        try {
                            val expression = smartParseQuantityWithDefaultUnit(text, default.unit.toEUnitLiteral())
                            checkDimension(name, text, reducer, expression, default.toEQuantityScale())
                            with(ToValue(ops)) { reducer.reduce(expression).toValue() }
                        } catch (e: EvaluatorException) {
                            throw e.within(ArgumentFrame(SourcePath.Argument(name), text))
                        }
                    }

                    is StringValue -> StringValue(stringOf(name, value))
                    else -> throw RequestException(ErrorBody("field '$name': column of an unsupported type"))
                }
            })
        }
    }

    /** A given quantity must have the dimension of the default value that it replaces. */
    private fun checkDimension(
        name: String,
        text: String,
        reducer: DataExpressionReducer<BasicNumber>,
        given: DataExpression<BasicNumber>,
        default: DataExpression<BasicNumber>,
    ) {
        if (default !is QuantityExpression<*>) return
        val expected = (with(ToValue(ops)) { reducer.reduce(default).toValue() } as? QuantityValue)?.unit?.dimension
        val actual = (with(ToValue(ops)) { reducer.reduce(given).toValue() } as? QuantityValue)?.unit?.dimension
        if (expected != null && actual != expected) {
            throw EvaluatorException("incompatible dimensions: expected $expected, found $actual")
                .within(ArgumentFrame(SourcePath.Argument(name), text))
        }
    }

    private fun stringOf(name: String, json: JsonElement): String =
        (json as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: throw RequestException(ErrorBody("field '$name': expected a string"))

    /**
     * The text of a quantity, given as text in the language syntax ("4 u"), a number in the default unit,
     * or an object with an amount and a unit.
     */
    private fun textOf(name: String, json: JsonElement): String = when {
        json is JsonPrimitive && json !is JsonNull && json.booleanOrNull == null -> json.content
        json is JsonObject && json.keys == setOf("amount", "unit") -> {
            val amount = (json["amount"] as? JsonPrimitive)?.doubleOrNull
            val unit = (json["unit"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            if (amount == null || unit == null) throw RequestException(ErrorBody("field '$name': expected a quantity"))
            "$amount $unit"
        }

        else -> throw RequestException(ErrorBody("field '$name': expected a quantity"))
    }

    private fun errorBody(e: EvaluatorException) = ErrorBody(
        e.message ?: "evaluation failed",
        e.frames.map { ErrorFrame(it.describe(e.sourceMap), location(e, it.path)) },
        e.related.map { ErrorFrame(it.describe(e.sourceMap), location(e, it.path)) },
    )

    private fun location(e: EvaluatorException, path: SourcePath): String? =
        if (showLocations) e.sourceMap[path]?.toString() else null
}

private fun TraceRow.matches(where: RowSelector): Boolean {
    val product = output as? ProductValue ?: return false
    val ref = product.fromProcessRef
    return (where.product == null || where.product == product.name) &&
        (where.process == null || where.process == ref?.name) &&
        where.labels.all { (k, v) -> ref?.matchLabels?.get(k)?.s == v } &&
        where.arguments.all { (k, v) -> (ref?.arguments?.get(k) as? StringValue)?.s == v }
}

private fun TraceRow.cells(select: List<String>): JsonObject {
    val product = output as? ProductValue
    val ref = product?.fromProcessRef
    return buildJsonObject {
        select.forEach { column ->
            when (column) {
                Columns.PRODUCT -> put(column, product?.name)
                Columns.PROCESS -> put(column, ref?.name)
                Columns.LABELS -> put(column, strings(ref?.matchLabels.orEmpty()))
                Columns.ARGUMENTS -> put(column, strings(ref?.arguments.orEmpty()))
                Columns.AMOUNT -> put(column, quantity(supply))
                else -> put(column, impacts.entries.firstOrNull { it.key.getShortName() == column }?.let { quantity(it.value) }
                    ?: JsonNull)
            }
        }
    }
}

private fun TraceRow.quantity(value: QuantityValue<BasicNumber>) = buildJsonObject {
    put("amount", value.amount.value * allocation)
    put("unit", value.unit.toString())
}

private fun strings(values: Map<String, DataValue<BasicNumber>>) =
    JsonObject(values.mapValues { JsonPrimitive(it.value.toString()) })
