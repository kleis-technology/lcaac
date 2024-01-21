package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.lang.Double.parseDouble
import java.nio.file.Paths

class BasicCsvSourceOperations(
    private val path: File,
) : DataSourceOperations<BasicNumber> {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()

    override fun readAll(source: DataSourceExpression<BasicNumber>): Sequence<ERecord<BasicNumber>> {
        return when (source) {
            is ECsvSource -> {
                val location = Paths.get(path.absolutePath, source.location)
                val inputStream = location.toFile().inputStream()
                val parser = CSVParser(inputStream.reader(), format)
                val header = parser.headerMap
                parser.iterator().asSequence()
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
        }
    }

    override fun sumProduct(
        source: DataSourceExpression<BasicNumber>,
        columns: List<String>,
    ): DataExpression<BasicNumber> {
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = BasicOperations,
            sourceOps = this,
        )
        return when (source) {
            is ECsvSource -> {
                val location = Paths.get(path.absolutePath, source.location)
                val inputStream = location.toFile().inputStream()
                val parser = CSVParser(inputStream.reader(), format)
                val header = parser.headerMap
                parser.iterator().asSequence()
                    .map { record ->
                        columns.map { column ->
                            val position = header[column]
                                ?: throw IllegalStateException(
                                    "${source.location}: invalid schema: unknown column '$column'"
                                )
                            val columnType = source.schema[column]
                                ?: throw IllegalStateException(
                                    "invalid schema: column '$column' has an invalid default value"
                                )
                            val defaultValue = columnType.defaultValue
                            val element = record[position]
                            parseQuantityWithDefaultUnit(element, EUnitOf(defaultValue))
                        }.reduce { acc, expression ->
                            reducer.reduce(EQuantityMul(acc, expression))
                        }
                    }.reduce { acc, expression ->
                        reducer.reduce(EQuantityAdd(acc, expression))
                    }
            }
        }
    }
    private fun parseQuantityWithDefaultUnit(s: String, defaultUnit: DataExpression<BasicNumber>):
        DataExpression<BasicNumber> {
        val amount = try {
            parseDouble(s)
        } catch (e: NumberFormatException) {
            throw EvaluatorException("'$s' is not a valid number")
        }
        return EQuantityScale(BasicNumber(amount), defaultUnit)
    }
}
