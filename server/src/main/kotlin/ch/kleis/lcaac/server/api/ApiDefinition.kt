package ch.kleis.lcaac.server.api

import kotlinx.serialization.Serializable

/**
 * The API served by `lcaac serve`, as declared under the key `api` of `lcaac.yaml`: operations, and how
 * they map onto the model.
 */
@Serializable
data class ApiDefinition(
    val name: String,
    val version: String,
    val description: String = "",
    val operations: Map<String, Operation> = emptyMap(),
)

/**
 * An operation evaluates [process] for one unit of its reference product, and returns cells of its trace table.
 */
@Serializable
data class Operation(
    val process: String,
    val description: String = "",
    val labels: Map<String, String> = emptyMap(),
    val inputs: Map<String, Input> = emptyMap(),
    val data: Map<String, DataInput> = emptyMap(),
    val outputs: Map<String, Output> = emptyMap(),
)

/**
 * A field of the request, given as the value of a [parameter] or a [label] of the process (exactly one of them).
 */
@Serializable
data class Input(
    val parameter: String? = null,
    val label: String? = null,
    val description: String = "",
)

/**
 * A field of the request whose rows replace the rows of [datasource].
 */
@Serializable
data class DataInput(
    val datasource: String,
    val description: String = "",
)

/**
 * A field of the response: the columns [select] of the rows of the trace table matching [where].
 */
@Serializable
data class Output(
    val select: List<String>,
    val where: RowSelector,
    val description: String = "",
)

/**
 * Conditions on the identity of a row of the trace table. Each given field narrows the selection.
 */
@Serializable
data class RowSelector(
    val product: String? = null,
    val process: String? = null,
    val labels: Map<String, String> = emptyMap(),
    val arguments: Map<String, String> = emptyMap(),
) {
    fun isEmpty(): Boolean = product == null && process == null && labels.isEmpty() && arguments.isEmpty()
}

object Columns {
    const val PRODUCT = "product"
    const val PROCESS = "process"
    const val LABELS = "labels"
    const val ARGUMENTS = "arguments"
    const val AMOUNT = "amount"

    val identity = listOf(PRODUCT, PROCESS, LABELS, ARGUMENTS)
    val fixed = identity + AMOUNT
}
