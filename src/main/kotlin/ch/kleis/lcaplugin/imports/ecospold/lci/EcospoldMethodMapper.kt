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
            validateHeaders(parser.headerMap)
            parser.stream().asSequence().mapNotNull { record ->
                when (record["flow_status"]) {
                    "ecoinvent orphan" -> {
                        record["id"] to MappingExchange.orphan(record["id"])
                    }

                    // Can be 'mapped', 'mapped: proxy' or 'mapped: compartment overwrite'.
                    // In all three cases, nullable fields sometimes exist and can be filled in.
                    else -> mappedElement(record)
                }
            }.toMap()
        }

    private fun validateHeaders(headers: Map<String, Int>) =
        sequenceOf("compartment_status",
            "conversion_factor",
            "flow_status",
            "id",
            "method_compartment",
            "method_name",
            "method_subcompartment",
            "method_unit",
            "name",
            "unitName")
            .forEach { header ->
                if (!headers.containsKey(header)) {
                    throw IllegalArgumentException("could not find $header in file headers. Is it a valid mapping file ?")
                }
            }

    private fun mappedElement(record: CSVRecord): Pair<ID, MappingExchange>? =
        try {
            val id = record["id"]
            id to MappingExchange(
                id,
                getConversionFactor(record["conversion_factor"]),
                record["method_name"].nullIfEmpty(),
                record["method_unit"].nullIfEmpty()?.let { pefUnitException(it, record["unitName"]) },
                record["method_compartment"].nullIfEmpty(),
                record["method_subcompartment"].nullIfEmpty(),
                "Ecoinvent ID: $id. Flow, compartment status: ${record["flow_status"]}, ${record["compartment_status"]}",
            )
        } catch (_: IllegalArgumentException) {
            null
        }

    fun getConversionFactor(factor: String): Double? =
        factor
            .nullIfEmpty()
            ?.toDoubleOrNull()
            ?.let {
                if (it == 1.0) null else it
            }

    private fun pefUnitException(methodUnit: String, unitName: String): String =
        if (unitName == "m2*year" && methodUnit == "m2*a") {
            unitName
        } else {
            methodUnit
        }

    private fun String.nullIfEmpty(): String? = this.ifEmpty { null }
}