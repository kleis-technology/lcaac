package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.InputStream
import java.lang.Double.parseDouble
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

enum class EnvVars(val key: String) {
    RESILIO_DB_URL("RESILIO_DB_URL"),
    RESILIO_DB_ACCESS_TOKEN("RESILIO_DB_ACCESS_TOKEN");
}

val yaml = Yaml(configuration = YamlConfiguration(
    strictMode = false
))
const val defaultLcaacFilename = "lcaac.yaml"

fun parseProjectPath(path: File): Pair<File, File> {
    if (path.isDirectory) {
        val configFile = Path(defaultLcaacFilename).toFile()
        return path to configFile
    }
    val workingDirectory = path.parentFile ?: Path(".").toFile()
    return workingDirectory to path
}

fun lcaFiles(root: File): Sequence<LcaLangParser.LcaFileContext> {
    return Files.walk(root.toPath())
        .filter { it.isRegularFile() }
        .filter { it.toString().endsWith(".lca") }
        .map { lcaFile(it.toFile().inputStream()) }
        .toList()
        .asSequence()
}

private fun lcaFile(inputStream: InputStream): LcaLangParser.LcaFileContext {
    val lexer = LcaLangLexer(CharStreams.fromStream(inputStream))
    val tokens = CommonTokenStream(lexer)
    val parser = LcaLangParser(tokens)
    return parser.lcaFile()
}

fun smartParseQuantityWithDefaultUnit(s: String, defaultUnit: DataExpression<BasicNumber>): DataExpression<BasicNumber> {
    val parts = s.split(" ")
    return if (parts.size == 1) {
        val number = parts[0]
        val amount = try {
            parseDouble(number)
        } catch (e: NumberFormatException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        }
        EQuantityScale(BasicNumber(amount), defaultUnit)
    } else {
        val lexer = LcaLangLexer(CharStreams.fromString(s))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val ctx = parser.dataExpression()
        try {
            CoreMapper(BasicOperations).dataExpression(ctx)
        } catch (e: IllegalStateException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        }
    }
}

fun prepareArguments(
    dataReducer: DataExpressionReducer<BasicNumber>,
    template: EProcessTemplate<BasicNumber>,
    request: Map<String, String>,
) = template.params.mapValues { entry ->
    when (val v = entry.value) {
        is QuantityExpression<*> -> request[entry.key]?.let {
            smartParseQuantityWithDefaultUnit(it, EUnitOf(v))
        } ?: entry.value

        is StringExpression -> request[entry.key]?.let {
            EStringLiteral(it)
        } ?: entry.value

        is EDefaultRecordOf -> {
            val dataSource = dataReducer.evalDataSource(v.dataSource)
            val schema = dataSource.schema
            val entries = schema.mapValues { schemaEntry ->
                when (val defaultValue = schemaEntry.value) {
                    is QuantityValue -> request[schemaEntry.key]?.let {
                        smartParseQuantityWithDefaultUnit(it, defaultValue.unit.toEUnitLiteral())
                    } ?: defaultValue.toEQuantityScale()

                    is StringValue -> request[schemaEntry.key]?.let {
                        EStringLiteral(it)
                    } ?: defaultValue.toEStringLiteral()

                    else -> throw EvaluatorException("datasource '${dataSource.config.name}': column '${schemaEntry
                        .key}': invalid default value")
                }
            }
            ERecord(entries)
        }

        else -> throw EvaluatorException("$v is not a supported data expression")
    }
}
