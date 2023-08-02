package ch.kleis.lcaplugin.imports.ecospold.lci

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ecospold.model.ElementaryExchange
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader
import kotlin.streams.asSequence

typealias ID = String

object EcospoldMapper {
    private val csvFormat: CSVFormat = CSVFormat.Builder.create().setHeader().build()

    fun buildMapping(mapData: Reader): Map<ID, ElementaryExchange> =
        CSVParser.parse(mapData, csvFormat).use { parser ->
            if (validateHeaders(parser.headerMap)) {
                parser.stream().asSequence().mapNotNull { record ->
                    when (record["flow_status"]) {
                        "mapped" -> mappedElement(record)
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
            && headers.containsKey("method_name")
            && headers.containsKey("method_unit")
            && headers.containsKey("method_compartment")
            && headers.containsKey("method_subcompartment")

    private fun mappedElement(record: CSVRecord): Pair<ID, ElementaryExchange>? =
        try {
            val id = record["id"]
            id to ElementaryExchange(
                id,
                record["conversion_factor"].toDouble(),
                record["method_name"],
                record["method_unit"],
                record["method_compartment"],
                record["method_subcompartment"],
                SubstanceType.EMISSION,
                "Mapped exchange"
            )
        } catch (_: IllegalArgumentException) {
            null
        }

}