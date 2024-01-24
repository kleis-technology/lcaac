package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.lang.Double.parseDouble
import java.nio.file.Paths

class CsvSourceOperations<Q>(
    private val path: File,
    private val ops: QuantityOperations<Q>,
) : DataSourceOperations<Q> {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()

    override fun readAll(source: EDataSource<Q>): Sequence<ERecord<Q>> {
        val location = Paths.get(path.absolutePath, source.location)
        val inputStream = location.toFile().inputStream()
        val parser = CSVParser(inputStream.reader(), format)
        val header = parser.headerMap
        val filter = source.filter
        return parser.iterator().asSequence()
            .filter { record ->
                filter.entries.all {
                    val iv = it.value
                    if (iv is EStringLiteral) {
                        val pos = header[it.key]
                            ?: throw IllegalStateException(
                                "${source.location}: invalid schema: unknown column '${it.key}'"
                            )
                        record[pos] == iv.value
                    } else throw EvaluatorException("invalid matching condition $it")
                }
            }
            .map { record ->
                val entries = header
                    .filter { entry -> source.schema.containsKey(entry.key) }
                    .mapValues { entry ->
                        val columnType = source.schema[entry.key]!!
                        val columnDefaultValue = columnType.defaultValue
                        val position = entry.value
                        val element = record[position]
                        when (columnDefaultValue) {
                            is QuantityExpression<*> ->
                                parseQuantityWithDefaultUnit(element, EUnitOf(columnDefaultValue))

                            is StringExpression ->
                                EStringLiteral(element)

                            else -> throw IllegalStateException(
                                "invalid schema: column '${entry.key}' has an invalid default value"
                            )
                        }
                    }
                ERecord(entries)
            }
    }

    override fun sumProduct(source: EDataSource<Q>, columns: List<String>): DataExpression<Q> {
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

    private fun parseQuantityWithDefaultUnit(s: String, defaultUnit: DataExpression<Q>):
        DataExpression<Q> {
        val amount = try {
            parseDouble(s)
        } catch (e: NumberFormatException) {
            throw EvaluatorException("'$s' is not a valid number")
        }
        return EQuantityScale(ops.pure(amount), defaultUnit)
    }
}
