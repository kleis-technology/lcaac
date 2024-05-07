package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.InputStream
import java.lang.Double.parseDouble
import java.nio.file.Paths

class CsvSourceOperations<Q>(
    private val path: File,
    private val ops: QuantityOperations<Q>,
    private val fileLoader: (String) -> InputStream = {
        val location = Paths.get(path.absolutePath, it)
        location.toFile().inputStream()
    }
) : DataSourceOperations<Q> {
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

    override fun readAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val records = load(source.location, source.schema)
        val filter = source.filter
        return records
            .filter { record ->
                filter.entries.all {
                    val expected = it.value
                    if (expected is StringValue) {
                        val actual = record.entries[it.key]
                            ?.let { with(ToValue(ops)) { it.toValue() } }
                            ?: throw IllegalStateException(
                                "${source.location}: invalid schema: unknown column '${it.key}'"
                            )
                        actual == expected
                    } else throw EvaluatorException("invalid matching condition $it")
                }
            }
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = ops,
            sourceOps = this,
        )
        return readAll(source).map { record ->
            columns.map { column ->
                record.entries[column]
                    ?: throw IllegalStateException(
                        "${source.location}: invalid schema: unknown column '$column'"
                    )
            }.reduce { acc, expression ->
                reducer.reduce(EQuantityMul(acc, expression))
            }
        }.reduce { acc, expression ->
            reducer.reduce(EQuantityAdd(acc, expression))
        }
    }

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        return readAll(source).firstOrNull()
            ?: throw EvaluatorException("no record found in '${source.location}' matching ${source.filter}")
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

