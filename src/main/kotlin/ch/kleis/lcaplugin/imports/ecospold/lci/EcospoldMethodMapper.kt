package ch.kleis.lcaplugin.imports.ecospold.lci

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader
import kotlin.streams.asSequence

typealias ID = String

data class MappingExchange(
    val elementaryExchangeId: String,
    val conversionFactor: Double? = null,
    val name: String? = null,
    val unit: String? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
    val comment: String = "",
) {
    companion object {
        fun orphan(id: ID) = MappingExchange(
            elementaryExchangeId = id,
            comment = "Ecoinvent orphan. Ecoinvent ID: $id"
        )

        fun unknown(id: ID) = MappingExchange(
            elementaryExchangeId = id,
            comment = "Empty method mapping. Ecoinvent ID: $id"
        )
    }
}

object EcospoldMethodMapper {
    private val csvFormat: CSVFormat = CSVFormat.Builder.create().setHeader().build()

    /* Flow status, compartment status values:
     * mapped, mapped -> all fields exist (when different from EcoInvent) and should be filled
     *
     * mapped, "" -> sometimes a method_compartment, no method_subcompartment though the substance in our library and
     *               in PEF nomenclature requires it.
     *
     * mapped[,:] overwrite, mapped -> all fields exist (when different from EcoInvent) and should be filled
     *
     * mapped[,:] overwrite, mapped: compartment overwrite -> all fields exist etc...
     *
     * mapped[,:] overwrite, mapped: proxy -> all fields exist etc...
     *
     * mapped[,:] overwrite, "" -> sometimes a method_compartment, no method_subcompartment though the substance in our
     *                             library and in PEF nomenclature requires it.
     */

    fun buildMapping(mapData: Reader): Map<ID, MappingExchange> =
        CSVParser.parse(mapData, csvFormat).use { parser ->
            validateHeaders(parser.headerMap)
            parser.stream().asSequence().mapNotNull { record ->
                when {
                    record["flow_status"] == "ecoinvent orphan" -> {
                        record["id"] to MappingExchange.orphan(record["id"])
                    }

                    record["compartment_status"].isEmpty() -> {
                        record["id"] to MappingExchange.unknown(record["id"])
                    }

                    else -> mappedElement(record)
                }
            }.toMap()
        }

    private fun validateHeaders(headers: Map<String, Int>) =
        sequenceOf(
            "compartment_status",
            "conversion_factor",
            "flow_status",
            "id",
            "method_compartment",
            "method_name",
            "method_subcompartment",
            "method_unit",
            "name",
            "unitName"
        )
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