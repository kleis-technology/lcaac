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
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
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
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.parseOverrides
import ch.kleis.lcaac.grammar.smartParseQuantityWithDefaultUnit
import ch.kleis.lcaac.server.api.Aggregate
import ch.kleis.lcaac.server.api.Columns
import ch.kleis.lcaac.server.api.ForEach
import ch.kleis.lcaac.server.api.Output
import ch.kleis.lcaac.server.api.UnitResolver
import ch.kleis.lcaac.server.api.Units
import ch.kleis.lcaac.server.api.Operation
import ch.kleis.lcaac.server.api.RowSelector
import ch.kleis.lcaac.server.api.satisfies
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

    /** [status] is 400 when the request cannot be read, 422 when the evaluation fails, 500 when the API does not fit the model. */
    data class Failure(val status: Int, val body: ErrorBody) : OperationResult
}

private class RequestException(val body: ErrorBody) : Exception(body.error)

/** [status] is 422 when the evaluation does not fit an output, 500 when the API does not fit the model. */
private class OutputException(val status: Int, message: String) : Exception(message)

/**
 * Runs the operations of an API on a model: maps a request onto the process, evaluates it, and returns
 * the selected cells of its trace table.
 */
class OperationRunner(
    private val symbolTable: SymbolTable<BasicNumber>,
    private val config: LcaacConfig,
    private val connectors: Map<String, DataSourceConnector<BasicNumber>>,
    units: Units,
    private val showLocations: Boolean = true,
) {
    private val ops = BasicOperations
    private val units = UnitResolver(symbolTable).resolve(units)

    /**
     * What the requests of a batch share: the reads of the data sources, and an evaluator per value of the
     * global parameters given, hence the processes evaluated once with `@cached`.
     */
    private inner class Session {
        val sourceOps = DefaultDataSourceOperations(ops, config, connectors)
        val reducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
        private val evaluators = HashMap<Map<String, DataValue<BasicNumber>>, Evaluator<BasicNumber, BasicMatrix>>()

        fun evaluator(prepared: Prepared): Evaluator<BasicNumber, BasicMatrix> =
            evaluators.getOrPut(prepared.globalValues) { Evaluator(prepared.symbolTable, ops, sourceOps) }

        fun reducer(symbolTable: SymbolTable<BasicNumber>) =
            DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
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
            prepare(operation, request, session)
        } catch (e: RequestException) {
            return OperationResult.Failure(400, e.body)
        } catch (e: EvaluatorException) {
            return OperationResult.Failure(400, errorBody(e))
        }
        val evaluator = evaluator(prepared, session)
        val forEach = operation.forEach
            // The request as sent, so that each result is self-contained, e.g. in a batch
            ?: return when (val outputs = evaluate(operation, evaluator, prepared.template, prepared.arguments)) {
                is OperationResult.Failure -> outputs
                is OperationResult.Success -> OperationResult.Success(buildJsonObject {
                    put("request", request)
                    put("outputs", outputs.body)
                })
            }

        val rows = try {
            rowsOf(operation, forEach, request, prepared, session)
        } catch (e: EvaluatorException) {
            return OperationResult.Failure(422, errorBody(e))
        }
        val results = rows.mapIndexed { index, (json, record) ->
            val arguments = prepared.arguments + forEach.arguments.mapValues { (_, column) ->
                expressionOf(record.entries[column]!!)
            }
            when (val outputs = evaluate(operation, evaluator, prepared.template, arguments)) {
                is OperationResult.Failure -> return OperationResult.Failure(
                    outputs.status,
                    outputs.body.copy(frames = outputs.body.frames + ErrorFrame("row '${forEach.rows}[$index]'")),
                )

                is OperationResult.Success -> buildJsonObject {
                    put("row", json)
                    put("outputs", outputs.body)
                }
            }
        }
        // Each row is repeated in its result, not in the request
        return OperationResult.Success(buildJsonObject {
            put("request", JsonObject(request - forEach.rows))
            put("results", JsonArray(results))
        })
    }

    private fun evaluator(prepared: Prepared, session: Session): Evaluator<BasicNumber, BasicMatrix> =
        // Rows given by the request replace a data source, which a cached process may read:
        // the evaluators of the session cannot be used
        if (prepared.rows.isEmpty()) session.evaluator(prepared) else Evaluator(
            prepared.rows.keys.fold(prepared.symbolTable) { st, name ->
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

    /**
     * The rows of a `for_each`, as sent and as values: those of the request, or else those of the model,
     * written as a request would give them.
     */
    private fun rowsOf(
        operation: Operation,
        forEach: ForEach,
        request: JsonObject,
        prepared: Prepared,
        session: Session,
    ): List<Pair<JsonElement, RecordValue<BasicNumber>>> {
        val dataSource = operation.data[forEach.rows]!!.datasource
        request[forEach.rows]?.let { json -> return (json as JsonArray).zip(prepared.rows[dataSource]!!) }
        val reducer = session.reducer(prepared.symbolTable)
        val source = reducer.evalDataSource(symbolTable.getDataSource(dataSource)!!)
        return session.sourceOps.getAll(source).map { record ->
            val value = RecordValue(record.entries.mapValues { (_, v) ->
                with(ToValue(ops)) { reducer.reduce(v).toValue() }
            })
            JsonObject(value.entries.mapValues { (_, v) ->
                when (v) {
                    is QuantityValue -> quantity(v.amount.value, v.unit)
                    else -> JsonPrimitive((v as? StringValue)?.s ?: v.toString())
                }
            }) to value
        }.toList()
    }

    private fun expressionOf(value: DataValue<BasicNumber>): DataExpression<BasicNumber> = when (value) {
        is QuantityValue -> value.toEQuantityScale()
        is StringValue -> EStringLiteral(value.s)
        else -> throw EvaluatorException("unsupported argument $value")
    }

    /** The outputs of [template] evaluated with [arguments], as the body of a success. */
    private fun evaluate(
        operation: Operation,
        evaluator: Evaluator<BasicNumber, BasicMatrix>,
        template: EProcessTemplate<BasicNumber>,
        arguments: Map<String, DataExpression<BasicNumber>>,
    ): OperationResult {
        val (trace, rows) = try {
            val trace = evaluator.trace(template, arguments)
            trace to TraceTableProgram(trace).run()
        } catch (e: EvaluatorException) {
            return OperationResult.Failure(422, errorBody(e))
        } catch (e: IllegalStateException) {
            return OperationResult.Failure(422, ErrorBody(e.message ?: e.toString()))
        } catch (e: IllegalArgumentException) {
            return OperationResult.Failure(422, ErrorBody(e.message ?: e.toString()))
        }

        val referenceRows = rows.takeWhile { it.demandedProduct == rows.first().demandedProduct }
        val outputs = operation.outputs.mapValues { (name, output) ->
            try {
                val matched = referenceRows.filter { it.matches(output.where) }
                when (output.aggregate) {
                    null -> JsonArray(matched.map { cells(it, output) })
                    Aggregate.ONE -> matched.singleOrNull()?.let { cells(it, output) } ?: throw OutputException(
                        422, "expected exactly one matching row, found ${matched.size}"
                    )

                    Aggregate.SUM -> sum(trace, matched, output)
                }
            } catch (e: OutputException) {
                return OperationResult.Failure(e.status, ErrorBody("output '$name': ${e.message}"))
            }
        }
        return OperationResult.Success(JsonObject(outputs))
    }

    /** The selected cells of [row], with quantities in the units of the API. */
    private fun cells(row: TraceRow, output: Output): JsonObject {
        val product = row.output as? ProductValue
        val ref = product?.fromProcessRef
        return buildJsonObject {
            output.select.forEach { column ->
                when (column) {
                    Columns.PRODUCT -> put(column, product?.name)
                    Columns.PROCESS -> put(column, ref?.name)
                    Columns.LABELS -> put(column, strings(ref?.matchLabels.orEmpty()))
                    Columns.ARGUMENTS -> put(column, strings(ref?.arguments.orEmpty()))
                    else -> {
                        val unit = unitOf(column, output)
                        put(column, quantity(amountIn(column, row, unit), unit))
                    }
                }
            }
        }
    }

    /** The sum of [rows] for each selected column, in the units of the API: 0 with no rows. */
    private fun sum(trace: EvaluationTrace<BasicNumber>, rows: List<TraceRow>, output: Output): JsonObject {
        upstreamRow(trace, rows)?.let {
            throw OutputException(
                422, "the matching row '${it.describe()}' is upstream of another one, and would be counted twice"
            )
        }
        return buildJsonObject {
            output.select.forEach { column ->
                val unit = unitOf(column, output)
                put(column, quantity(rows.sumOf { amountIn(column, it, unit) }, unit))
            }
        }
    }

    /** The unit of the API for a column of quantities, required by the checker. */
    private fun unitOf(column: String, output: Output): UnitValue<BasicNumber> =
        (if (column == Columns.AMOUNT) units.products[output.where.product] else units.impacts[column])
            ?: throw OutputException(500, "no unit for column '$column'")

    /**
     * The amount of [column] in [row], allocated, in [unit]: 0 when the evaluated system has no such column,
     * i.e. no contribution.
     */
    private fun amountIn(column: String, row: TraceRow, unit: UnitValue<BasicNumber>): Double {
        val value = row.column(column) ?: return 0.0
        if (value.unit.dimension != unit.dimension) throw OutputException(
            500, "column '$column': the model gives ${value.unit}, which does not convert into $unit, the unit of the API"
        )
        return value.amount.value * value.unit.scale * row.allocation / unit.scale
    }

    /** A row of [rows] upstream of another one, if any: the supply chains of the rows overlap. */
    private fun upstreamRow(trace: EvaluationTrace<BasicNumber>, rows: List<TraceRow>): TraceRow? {
        val supplier = trace.getSystemValue().productToProcessMap
        val byPort = rows.associateBy { it.output }
        fun inputsOf(port: MatrixColumnIndex<BasicNumber>) =
            (port as? ProductValue)?.let { supplier[it] }?.inputs.orEmpty().map { it.product }
        val visited = HashSet<MatrixColumnIndex<BasicNumber>>()
        val stack = ArrayDeque(byPort.keys.flatMap(::inputsOf))
        while (stack.isNotEmpty()) {
            val port = stack.removeLast()
            if (!visited.add(port)) continue
            byPort[port]?.let { return it }
            stack.addAll(inputsOf(port))
        }
        return null
    }

    /** A request read: [symbolTable] with the global parameters given, whose values are [globalValues]. */
    private class Prepared(
        val symbolTable: SymbolTable<BasicNumber>,
        val globalValues: Map<String, DataValue<BasicNumber>>,
        val template: EProcessTemplate<BasicNumber>,
        val arguments: Map<String, DataExpression<BasicNumber>>,
        val rows: Map<String, List<RecordValue<BasicNumber>>>,
    )

    private fun prepare(operation: Operation, request: JsonObject, session: Session): Prepared {
        request.keys.firstOrNull { it !in operation.inputs && it !in operation.data }?.let {
            throw RequestException(ErrorBody("unknown field '$it'"))
        }

        // The global parameters first: the other fields are read under their values
        val globalInputs = operation.inputs.filterValues { it.global != null }
        val globals = values(
            request,
            globalInputs.mapValues { symbolTable.getGlobalParameter(it.value.global!!)!! },
            session.reducer,
        ).mapKeys { globalInputs[it.key]!!.global!! }
        val symbolTable = symbolTable.overrideGlobalParameters(globals)
        val reducer = if (globals.isEmpty()) session.reducer else session.reducer(symbolTable)
        val globalValues = globals.mapValues { with(ToValue(ops)) { reducer.reduce(it.value).toValue() } }

        val labels = operation.labels + operation.inputs
            .filterValues { it.label != null }
            .mapNotNull { (name, input) -> request[name]?.let { input.label!! to stringOf(name, it) } }
        val template = this.symbolTable.getTemplate(operation.process, labels)
            ?: throw RequestException(ErrorBody("no process '${operation.process}' with labels $labels"))

        val parameterInputs = operation.inputs.filterValues { it.parameter != null && it.parameter in template.params }
        val arguments = values(request, parameterInputs.mapValues { template.params[it.value.parameter]!! }, reducer)

        val rows = operation.data.mapNotNull { (name, data) ->
            request[name]?.let { data.datasource to records(name, data.datasource, it, reducer) }
        }.toMap()
        return Prepared(
            symbolTable, globalValues, template, arguments.mapKeys { parameterInputs[it.key]!!.parameter!! }, rows,
        )
    }

    /**
     * The values given by [request] to the fields of [defaults], with the type and dimension of their default.
     * Parsed under the names of the request, so that errors name the fields of the request.
     */
    private fun values(
        request: JsonObject,
        defaults: Map<String, DataExpression<BasicNumber>>,
        reducer: DataExpressionReducer<BasicNumber>,
    ): Map<String, DataExpression<BasicNumber>> {
        val given = defaults.keys.mapNotNull { name -> request[name]?.let { name to it } }.toMap()
        given.forEach { (name, value) -> if (defaults[name] is EStringLiteral<*>) stringOf(name, value) }
        val values = parseOverrides(reducer, given.keys.associateWith { defaults[it]!! }, given.mapValues { textOf(it.key, it.value) })
        values.forEach { (name, value) -> checkDimension(name, textOf(name, given[name]!!), reducer, value, defaults[name]!!) }
        return values
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
        where.labels.all { (k, condition) -> satisfies(ref?.matchLabels?.get(k)?.s, condition) } &&
        where.arguments.all { (k, condition) ->
            satisfies(ref?.arguments?.get(k)?.let { (it as? StringValue)?.s ?: it.toString() }, condition)
        }
}

/** The quantity of a column of quantities, before allocation. */
private fun TraceRow.column(column: String): QuantityValue<BasicNumber>? = when (column) {
    Columns.AMOUNT -> supply
    else -> impacts.entries.firstOrNull { it.key.getShortName() == column }?.value
}

private fun TraceRow.describe(): String {
    val product = output as? ProductValue ?: return output.getShortName()
    val ref = product.fromProcessRef ?: return product.name
    val labels = ref.matchLabels.entries.joinToString(", ", "{", "}") { "${it.key}=${it.value.s}" }
    val arguments = ref.arguments.entries.joinToString(", ", "(", ")") { "${it.key}=${it.value}" }
    return "${product.name} from ${ref.name}${if (ref.matchLabels.isEmpty()) "" else labels}$arguments"
}

private fun quantity(amount: Double, unit: UnitValue<BasicNumber>) = buildJsonObject {
    put("amount", amount)
    put("unit", unit.toString())
}

private fun strings(values: Map<String, DataValue<BasicNumber>>) =
    JsonObject(values.mapValues { JsonPrimitive(it.value.toString()) })
