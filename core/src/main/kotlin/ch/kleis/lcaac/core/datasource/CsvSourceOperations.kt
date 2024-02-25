package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
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
) : DataSourceOperationsBase<Q>(ops, { description ->
    val inputStream = fileLoader(description.location)
    val parser = CSVParser(inputStream.reader(), format)
    val header = parser.headerMap
    val schema = description.schema
    parser.iterator().asSequence()
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
})

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

