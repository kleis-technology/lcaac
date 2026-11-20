package ch.kleis.lcaac.server.api

import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlinx.serialization.json.*

/**
 * The OpenAPI document of an API, derived from its definition and the model. It describes the contract
 * only: nothing of how the operations map onto the model.
 */
class OpenApiGenerator(private val symbolTable: SymbolTable<BasicNumber>) {
    private val checker = ApiChecker(symbolTable)
    private val reducer = DataExpressionReducer(
        symbolTable.data, symbolTable.dataSources, BasicOperations, DummySourceOperations(),
    )

    private val unitResolver = UnitResolver(symbolTable)

    fun generate(definition: ApiDefinition): JsonObject {
        val units = unitResolver.resolve(definition.units)
        val schemas = mutableMapOf<String, JsonObject>()
        val paths = buildJsonObject {
            definition.operations.forEach { (name, operation) ->
                putJsonObject("/$name") {
                    putJsonObject("post") { operation(name, operation, units, schemas) }
                }
                putJsonObject("/$name/batch") {
                    putJsonObject("post") { batch(name, operation, schemas) }
                }
            }
        }
        return buildJsonObject {
            put("openapi", "3.0.3")
            putJsonObject("info") {
                put("title", definition.name)
                put("version", definition.version)
                if (definition.description.isNotEmpty()) put("description", definition.description)
            }
            put("paths", paths)
            putJsonObject("components") {
                put("schemas", JsonObject(schemas + commonSchemas))
            }
        }
    }

    private fun JsonObjectBuilder.operation(
        name: String,
        operation: Operation,
        units: ResolvedUnits,
        schemas: MutableMap<String, JsonObject>,
    ) {
        val prefix = pascal(name)
        put("operationId", name)
        if (operation.description.isNotEmpty()) put("summary", operation.description)

        val template = checker.candidates(operation).first()
        schemas["${prefix}Request"] = buildJsonObject {
            put("type", "object")
            put("additionalProperties", false)
            putJsonObject("properties") {
                operation.inputs.forEach { (field, input) ->
                    put(field, inputSchema(operation, template, input))
                }
                operation.data.forEach { (field, data) ->
                    val rowSchema = "$prefix${pascal(field)}Row"
                    schemas[rowSchema] = rowSchema(data.datasource)
                    putJsonObject(field) {
                        put("type", "array")
                        put("description", data.description.ifEmpty {
                            "Rows to assess. Without them, the default rows of the model are used."
                        })
                        putJsonObject("items") { put("\$ref", "#/components/schemas/$rowSchema") }
                    }
                }
            }
        }
        val outputs = buildJsonObject {
            put("type", "object")
            putJsonArray("required") { operation.outputs.keys.forEach { add(it) } }
            putJsonObject("properties") {
                operation.outputs.forEach { (field, output) -> put(field, outputSchema(output, units)) }
            }
        }
        val forEach = operation.forEach
        schemas["${prefix}Response"] = if (forEach == null) buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("request"); add("outputs") }
            putJsonObject("properties") {
                putJsonObject("request") {
                    putJsonArray("allOf") { add(ref("${prefix}Request")) }
                    put("description", "The request, as sent.")
                }
                put("outputs", outputs)
            }
        } else buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("request"); add("results") }
            putJsonObject("properties") {
                putJsonObject("request") {
                    putJsonArray("allOf") { add(ref("${prefix}Request")) }
                    put("description", "The request, as sent, without '${forEach.rows}'.")
                }
                putJsonObject("results") {
                    put("type", "array")
                    put("description", "One result per row of '${forEach.rows}', in the same order.")
                    putJsonObject("items") {
                        put("type", "object")
                        putJsonArray("required") { add("row"); add("outputs") }
                        putJsonObject("properties") {
                            putJsonObject("row") {
                                putJsonArray("allOf") { add(ref("$prefix${pascal(forEach.rows)}Row")) }
                                put("description", "The row, as sent.")
                            }
                            put("outputs", outputs)
                        }
                    }
                }
            }
        }

        putJsonObject("requestBody") {
            put("required", false)
            putJsonObject("content") {
                putJsonObject("application/json") {
                    putJsonObject("schema") { put("\$ref", "#/components/schemas/${prefix}Request") }
                }
            }
        }
        putJsonObject("responses") {
            put("200", response("Results", "${prefix}Response"))
            put("400", response("A value of the request cannot be read, or has the wrong dimension", "Error"))
            put("422", response("The evaluation failed", "Error"))
            put("500", response("The API does not fit the model", "Error"))
        }
    }

    private fun JsonObjectBuilder.batch(name: String, operation: Operation, schemas: MutableMap<String, JsonObject>) {
        val prefix = pascal(name)
        put("operationId", "${name}_batch")
        put("summary", listOf(operation.description, "Several requests at once.").filter { it.isNotEmpty() }.joinToString(" "))
        schemas["${prefix}BatchRequest"] = buildJsonObject {
            put("type", "object")
            put("additionalProperties", false)
            putJsonArray("required") { add("requests") }
            putJsonObject("properties") {
                putJsonObject("requests") {
                    put("type", "array")
                    put("items", ref("${prefix}Request"))
                }
            }
        }
        schemas["${prefix}BatchResponse"] = buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("results") }
            putJsonObject("properties") {
                putJsonObject("results") {
                    put("type", "array")
                    put("description", "One result per request, in the order of the requests.")
                    put("items", ref("${prefix}Response"))
                }
            }
        }
        putJsonObject("requestBody") {
            put("required", true)
            putJsonObject("content") {
                putJsonObject("application/json") { put("schema", ref("${prefix}BatchRequest")) }
            }
        }
        putJsonObject("responses") {
            put("200", response("One result per request", "${prefix}BatchResponse"))
            put("400", response("A request cannot be read, or the batch is too large", "Error"))
            put("422", response("The evaluation of a request failed", "Error"))
            put("500", response("The API does not fit the model", "Error"))
        }
    }

    private fun inputSchema(operation: Operation, template: EProcessTemplate<BasicNumber>, input: Input): JsonObject =
        when {
            input.label != null -> buildJsonObject {
                put("type", "string")
                putJsonArray("enum") { checker.labelValues(operation, input.label).forEach { add(it) } }
                if (input.description.isNotEmpty()) put("description", input.description)
            }

            else -> valueSchema(value(template.params[input.parameter]!!), input.description)
        }

    private fun rowSchema(dataSource: String): JsonObject {
        val schema = reducer.evalDataSource(symbolTable.getDataSource(dataSource)!!).schema
        return buildJsonObject {
            put("type", "object")
            put("additionalProperties", false)
            putJsonObject("properties") {
                schema.forEach { (column, default) -> put(column, valueSchema(default, "")) }
            }
        }
    }

    private fun valueSchema(default: DataValue<BasicNumber>?, description: String): JsonObject = when (default) {
        is QuantityValue -> buildJsonObject {
            putJsonArray("allOf") { addJsonObject { put("\$ref", "#/components/schemas/QuantityInput") } }
            val unit = default.unit
            put("description", listOf(
                description,
                "Quantity of dimension ${unit.dimension}. Default unit ${unit.symbol}.",
            ).filter { it.isNotEmpty() }.joinToString(" "))
            put("default", default.toString())
        }

        is StringValue -> buildJsonObject {
            put("type", "string")
            if (description.isNotEmpty()) put("description", description)
            put("default", default.s)
        }

        else -> buildJsonObject {
            if (description.isNotEmpty()) put("description", description)
        }
    }

    private fun outputSchema(output: Output, units: ResolvedUnits): JsonObject {
        val row = buildJsonObject {
            put("type", "object")
            putJsonArray("required") { output.select.forEach { add(it) } }
            putJsonObject("properties") {
                output.select.forEach { column ->
                    put(column, when (column) {
                        Columns.PRODUCT, Columns.PROCESS -> buildJsonObject { put("type", "string") }
                        Columns.LABELS, Columns.ARGUMENTS -> ref("Strings")
                        else -> quantitySchema(
                            if (column == Columns.AMOUNT) units.products[output.where.product] else units.impacts[column]
                        )
                    })
                }
            }
        }
        val description = output.description.ifEmpty {
            when (output.aggregate) {
                null -> "One element per matching row of the trace."
                Aggregate.SUM -> "The sum of the matching rows of the trace."
                Aggregate.ONE -> "The only matching row of the trace."
            }
        }
        return when (output.aggregate) {
            null -> buildJsonObject {
                put("type", "array")
                put("description", description)
                put("items", row)
            }

            else -> JsonObject(row + ("description" to JsonPrimitive(description)))
        }
    }

    /** A quantity in [unit], the unit of the API. */
    private fun quantitySchema(unit: UnitValue<BasicNumber>?) = if (unit == null) ref("Quantity") else buildJsonObject {
        put("type", "object")
        putJsonArray("required") { add("amount"); add("unit") }
        putJsonObject("properties") {
            putJsonObject("amount") { put("type", "number") }
            putJsonObject("unit") {
                put("type", "string")
                putJsonArray("enum") { add(unit.toString()) }
            }
        }
    }

    private fun value(expression: DataExpression<BasicNumber>): DataValue<BasicNumber>? = try {
        with(ToValue(BasicOperations)) { reducer.reduce(expression).toValue() }
    } catch (e: Exception) {
        null
    }

    private fun response(description: String, schema: String) = buildJsonObject {
        put("description", description)
        putJsonObject("content") {
            putJsonObject("application/json") { put("schema", ref(schema)) }
        }
    }

    private fun ref(schema: String) = buildJsonObject { put("\$ref", "#/components/schemas/$schema") }

    private val commonSchemas: Map<String, JsonObject> = mapOf(
        "QuantityInput" to buildJsonObject {
            put("description", "Text in the language syntax (\"4 u\"), a number in the default unit, or an amount with a unit.")
            putJsonArray("oneOf") {
                addJsonObject { put("type", "string") }
                addJsonObject { put("type", "number") }
                add(ref("Quantity"))
            }
        },
        "Quantity" to buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("amount"); add("unit") }
            putJsonObject("properties") {
                putJsonObject("amount") { put("type", "number") }
                putJsonObject("unit") { put("type", "string") }
            }
        },
        "Strings" to buildJsonObject {
            put("type", "object")
            putJsonObject("additionalProperties") { put("type", "string") }
        },
        "ErrorFrame" to buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("description") }
            putJsonObject("properties") {
                putJsonObject("description") { put("type", "string") }
                putJsonObject("location") { put("type", "string") }
            }
        },
        "Error" to buildJsonObject {
            put("type", "object")
            putJsonArray("required") { add("error") }
            putJsonObject("properties") {
                putJsonObject("error") { put("type", "string") }
                putJsonObject("frames") { put("type", "array"); put("items", ref("ErrorFrame")) }
                putJsonObject("related") { put("type", "array"); put("items", ref("ErrorFrame")) }
            }
        },
    )
}

private fun pascal(name: String): String =
    name.split('_', '-', '.').filter { it.isNotEmpty() }.joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
