package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.InputStream
import java.lang.Double.parseDouble

class CsvConnector<Q>(
    private val config: ConnectorConfig,
    private val ops: QuantityOperations<Q>,
    private val fileLoader: (String) -> InputStream,
) : DataSourceConnector<Q> {

    private fun csvRecords(location: String): Pair<Map<String, Int>, Sequence<CSVRecord>> {
        val inputStream = fileLoader(location)
        val parser = CSVParser(inputStream.reader(), format)
        val header = parser.headerMap
        return header to parser.iterator().asSequence()
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val location = config.location
            ?: throw EvaluatorException("Missing location in configuration for datasource '${config.name}'")
        val (header, csvRecords) = csvRecords(location)
        val records = csvRecords
            .filter(applyFilter(header, source.filter))
            .map { eRecord(ops, header, source.schema, it) }
        return records
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(config, source).firstOrNull()
            ?: throw EvaluatorException("no record found in datasource '${config.name}' [${config.location}] matching" +
                " ${source.filter}")
    }

    override fun getName(): String {
        return CsvConnectorKeys.CSV_CONNECTOR_NAME
    }

    override fun getConfig(): ConnectorConfig {
        return config
    }
}

private val format = CSVFormat.DEFAULT.builder()
    .setHeader()
    .setSkipHeaderRecord(true)
    .build()

private fun <Q> parseQuantityWithDefaultUnit(ops: QuantityOperations<Q>, s: String, defaultUnit: DataExpression<Q>):
    DataExpression<Q> {
    val amount = try {
        parseDouble(s)
    } catch (e: NumberFormatException) {
        throw EvaluatorException("'$s' is not a valid number")
    }
    return EQuantityScale(ops.pure(amount), defaultUnit)
}

private fun <Q> applyFilter(
    header: Map<String, Int>,
    filter: Map<String, DataValue<Q>>,
): (CSVRecord) -> Boolean = { record ->
    filter.entries.all {
        val expected = it.value
        if (expected is StringValue) {
            val position = header[it.key]
                ?: throw EvaluatorException("Unknown column '${it.key}'")
            val actual = record[position]
            actual == expected.s
        } else throw EvaluatorException("invalid matching condition $it")
    }
}

private fun <Q> eRecord(ops: QuantityOperations<Q>, header: Map<String, Int>, schema: Map<String, DataValue<Q>>, csvRecord: CSVRecord): ERecord<Q> {
    val entries = header
        .filter { entry -> schema.containsKey(entry.key) }
        .mapValues { entry ->
            val columnDefaultValue = schema[entry.key]!!
            val position = entry.value
            val element = csvRecord[position]
            when (columnDefaultValue) {
                is QuantityValue -> parseQuantityWithDefaultUnit(ops, element, columnDefaultValue.unit.toEUnitLiteral())
                is StringValue -> EStringLiteral(element)
                else -> throw IllegalStateException(
                    "invalid schema: column '${entry.key}' has an invalid default value"
                )
            }
        }
    return ERecord(entries)
}

