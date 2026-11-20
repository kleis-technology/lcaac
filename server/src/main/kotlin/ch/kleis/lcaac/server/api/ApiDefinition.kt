package ch.kleis.lcaac.server.api

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The API served by `lcaac serve`, as declared under the key `api` of `lcaac.yaml`: operations, and how
 * they map onto the model.
 */
@Serializable
data class ApiDefinition(
    val name: String,
    val version: String,
    val description: String = "",
    val units: Units = Units(),
    val operations: Map<String, Operation> = emptyMap(),
)

/**
 * The units of the quantities of the responses, in the syntax of the language: of the indicators and
 * substances, and of the amounts of each product. The values of the model are converted into them.
 */
@Serializable
data class Units(
    val impacts: Map<String, String> = emptyMap(),
    val products: Map<String, String> = emptyMap(),
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
    @SerialName("for_each")
    val forEach: ForEach? = null,
    val outputs: Map<String, Output> = emptyMap(),
)

/**
 * Evaluates the process once per row of the field [rows] of `data`, with [arguments]: a parameter of the
 * process for each column of the row. The rows replace the data source once for all of them.
 */
@Serializable
data class ForEach(
    val rows: String,
    val arguments: Map<String, String> = emptyMap(),
)

/**
 * A field of the request, given as the value of a [parameter] or a [label] of the process, or of a [global]
 * parameter of the model, for this request only (exactly one of them).
 */
@Serializable
data class Input(
    val parameter: String? = null,
    val label: String? = null,
    val global: String? = null,
    val description: String = "",
)

/**
 * A field of the request whose rows replace the rows of [datasource]. With [columns], a row gives exactly
 * these columns, under the names of the request; otherwise all the columns, under their own names.
 */
@Serializable
data class DataInput(
    val datasource: String,
    val description: String = "",
    val columns: Map<String, String> = emptyMap(),
) {
    /** The columns of a row: the name in the request, and the column of the data source among [schema]. */
    fun columnsOf(schema: Collection<String>): Map<String, String> =
        columns.ifEmpty { schema.associateWith { it } }
}

/**
 * A field of the response: the columns [select] of the rows of the trace table matching [where], one element
 * per row, or a single element with [aggregate].
 */
@Serializable
data class Output(
    val select: List<String>,
    val where: RowSelector,
    val aggregate: Aggregate? = null,
    val description: String = "",
)

@Serializable
enum class Aggregate {
    /** The sum of the rows, none of which may be upstream of another, lest it be counted twice. */
    @SerialName("sum")
    SUM,

    /** The only row: exactly one row must match. */
    @SerialName("one")
    ONE,
}

/**
 * Conditions on the identity of a row of the trace table. Each given field narrows the selection. A label or
 * an argument has one of the values of its condition, or is absent when its condition is null.
 */
@Serializable
data class RowSelector(
    val product: String? = null,
    val process: String? = null,
    val labels: Map<String, Condition?> = emptyMap(),
    val arguments: Map<String, Condition?> = emptyMap(),
) {
    fun isEmpty(): Boolean = product == null && process == null && labels.isEmpty() && arguments.isEmpty()
}

/**
 * A condition on a label or an argument: one of [values] (`phase: embodied`, `phase: [embodied, use]`).
 */
@Serializable(with = ConditionSerializer::class)
data class Condition(val values: List<String>)

/** Whether [value], absent when null, satisfies [condition], which requires an absent value when null. */
fun satisfies(value: String?, condition: Condition?): Boolean =
    if (condition == null) value == null else value in condition.values

/** Reads a condition from a scalar or a list of scalars. Not written. */
object ConditionSerializer : KSerializer<Condition> {
    // Contextual, so that kaml accepts any node
    @OptIn(kotlinx.serialization.InternalSerializationApi::class, kotlinx.serialization.ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("Condition", SerialKind.CONTEXTUAL)

    override fun deserialize(decoder: Decoder): Condition {
        // A contextual structure gives the node of the value, whatever its type
        val input = decoder.beginStructure(descriptor) as? YamlInput
            ?: throw SerializationException("a condition is read from YAML only")
        val node = input.node
        input.endStructure(descriptor)
        return when (node) {
            is YamlScalar -> Condition(listOf(node.content))
            is YamlList -> Condition(node.items.map {
                (it as? YamlScalar)?.content ?: throw SerializationException("expected a value or a list of values")
            })

            else -> throw SerializationException("expected a value or a list of values")
        }
    }

    override fun serialize(encoder: Encoder, value: Condition) =
        throw SerializationException("a condition is not written")
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
