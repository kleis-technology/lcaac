package ch.kleis.lcaplugin.imports.ecospold.lci

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader
import kotlin.streams.asSequence

typealias ID = String

data class MappingExchange(
    val elementaryExchangeId: String,
    val conversionFactor: Double?,
    val name: String?,
    val unit: String?,
    val compartment: String?,
    val subCompartment: String?,
    val comment: String,
) {
    companion object {
        fun orphan(id: ID) = MappingExchange(
            id,
            null,
            null,
            null,
            null,
            null,
            "Ecoinvent orphan. Ecoinvent ID: $id")
    }
}

object EcospoldMethodMapper {
    private val csvFormat: CSVFormat = CSVFormat.Builder.create().setHeader().build()

    fun buildMapping(mapData: Reader): Map<ID, MappingExchange> =
        CSVParser.parse(mapData, csvFormat).use { parser ->
            if (validateHeaders(parser.headerMap)) {
                parser.stream().asSequence().mapNotNull { record ->
                    when (record["flow_status"]) {
                        "mapped" -> mappedElement(record)
                        "ecoinvent orphan" -> {
                            record["id"] to MappingExchange.orphan(record["id"])
                        }

                        else -> null
                    }
                }.toMap()
            } else {
                throw IllegalArgumentException("Header validation failed.")
            }
        }

    private fun validateHeaders(headers: Map<String, Int>): Boolean =
        headers.containsKey("flow_status")
            && headers.containsKey("id")
            && headers.containsKey("conversion_factor")
            && headers.containsKey("name")
            && headers.containsKey("method_name")
            && headers.containsKey("unitName")
            && headers.containsKey("method_unit")
            && headers.containsKey("method_compartment")
            && headers.containsKey("method_subcompartment")

    private fun mappedElement(record: CSVRecord): Pair<ID, MappingExchange>? =
        try {
            val id = record["id"]
            id to MappingExchange(
                id,
                getConversionFactor(record["conversion_factor"]),
                record["method_name"].let { it.ifEmpty { record["name"] } },
                record["method_unit"].let { it.ifEmpty { record["unitName"] } },
                record["method_compartment"],
                record["method_subcompartment"].let { it.ifEmpty { null } },
                "Ecoinvent ID: $id",
            )
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun getConversionFactor(factor: String): Double? =
        factor
            .ifEmpty { null }
            ?.toDoubleOrNull()
            ?.let {
                if (it == 1.0) null else it
            }
}