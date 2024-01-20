package ch.kleis.lcaac.cli.csv

import ch.kleis.lcaac.cli.cmd.parseQuantityWithDefaultUnit
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
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

    override fun sum(source: DataSourceExpression<BasicNumber>, column: String): DataExpression<BasicNumber> {
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
                val position = header[column]
                    ?: throw IllegalStateException(
                        "${source.location}: invalid schema: unknown column '$column'"
                    )
                val columnType = source.schema[column]
                    ?: throw IllegalStateException(
                        "invalid schema: column '$column' has an invalid default value"
                    )
                val defaultValue = columnType.defaultValue
                parser.iterator().asSequence()
                    .map { record ->
                        val element = record[position]
                        parseQuantityWithDefaultUnit(element, defaultValue)
                    }.reduce { acc, expression ->
                        reducer.reduce(EQuantityAdd(acc, expression))
                    }
            }
        }
    }
}
