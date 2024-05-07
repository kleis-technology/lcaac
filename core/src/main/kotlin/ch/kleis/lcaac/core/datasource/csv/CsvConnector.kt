package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
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
import java.io.InputStream
import java.lang.Double.parseDouble
import java.nio.file.Paths

class CsvConnector<Q>(
    private val connectorConfig: CsvConnectorConfig,
    private val ops: QuantityOperations<Q>,
    private val fileLoader: (String) -> InputStream = { location ->
        val csvFile = Paths.get(connectorConfig.directory.absolutePath, location)
        csvFile.toFile().inputStream()
    }
) : DataSourceConnector<Q> {
    private fun load(location: String, schema: Map<String, DataValue<Q>>): Sequence<ERecord<Q>> {
        val inputStream = fileLoader(location)
        val parser = CSVParser(inputStream.reader(), format)
        val header = parser.headerMap
        return parser.iterator().asSequence()
            .map { csvRecord ->
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
                ERecord(entries)
            }
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val records = load(config.location, source.schema)
        val filter = source.filter
        return records
            .filter { record ->
                filter.entries.all {
                    val expected = it.value
                    if (expected is StringValue) {
                        val actual = record.entries[it.key]
                            ?.let { with(ToValue(ops)) { it.toValue() } }
                            ?: throw IllegalStateException(
                                "${source.config.name}: invalid schema: unknown column '${it.key}'"
                            )
                        actual == expected
                    } else throw EvaluatorException("invalid matching condition $it")
                }
            }
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(config, source).firstOrNull()
            ?: throw EvaluatorException("no record found in '${config.location}' matching ${source.filter}")
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

