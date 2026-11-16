package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.SyntaxException
import ch.kleis.lcaac.grammar.parseDataExpression
import ch.kleis.lcaac.grammar.parseLcaFile
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import org.apache.commons.compress.archivers.examples.Expander
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Double.parseDouble
import java.nio.file.Files
import kotlin.io.path.isRegularFile


val yaml = Yaml(configuration = YamlConfiguration(
    strictMode = false
))
const val defaultLcaacFilename = "lcaac.yaml"

fun parseSource(path: File): File {
    if (path.isDirectory) return path

    val name = path.name.lowercase()
    return when {
        name.endsWith(".zip") -> extractZip(path)
        name.endsWith(".tar.gz") || name.endsWith(".tgz") -> extractTarGz(path)
        else ->
            error("Unsupported file format: ${path.name}. Supported file formats are zip, tar.gz and tgz.")
    }
}

private fun extractZip(zipFile: File): File {
    val outputDir = Files.createTempDirectory("lca_zip_source_").toFile()
    Expander().expand(zipFile, outputDir)
    return outputDir
}

private fun extractTarGz(tarGzFile: File): File {
    val outputDir = Files.createTempDirectory("lca_targz_source_").toFile()
    TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(tarGzFile)))).use { tarInput ->
        var entry = tarInput.nextEntry
        while (entry != null) {
            val outputFile = File(outputDir, entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile.mkdirs()
                outputFile.outputStream().use { output ->
                    tarInput.copyTo(output)
                }
            }
            entry = tarInput.nextEntry
        }
    }
    return outputDir
}

fun parseLcaacConfig(path: File): LcaacConfig {
    return if (path.exists()) path.inputStream().use {
        yaml.decodeFromStream(LcaacConfig.serializer(), it)
    } else LcaacConfig()
}

/**
 * Parses the LCA files under [root]. When [root] is where the archive [source] is extracted, the files
 * are named after the archive in the locations of errors, e.g. `model.zip!/src/main.lca`.
 */
fun lcaFiles(root: File, source: File = root): Sequence<LcaLangParser.LcaFileContext> {
    val rootPath = root.toPath()
    return Files.walk(rootPath)
        .filter { it.isRegularFile() }
        .filter { it.toString().endsWith(".lca") }
        .map { path ->
            val sourceName = if (source == root) path.toString() else "${source.path}!/${rootPath.relativize(path)}"
            parseLcaFile(path, sourceName)
        }
        .toList()
        .asSequence()
}

fun <Q> dataExpressionMap(
    ops: QuantityOperations<Q>,
    globals: Map<String, String>,
): Map<DataKey, DataExpression<Q>> {
    val mapper = CoreMapper(ops)
    return globals
        .mapKeys { DataKey(it.key) }
        .mapValues {
            mapper.dataExpression(parseDataExpression(it.value, "<global ${it.key.name}>"))
        }
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
        try {
            CoreMapper(BasicOperations).dataExpression(parseDataExpression(s))
        } catch (e: SyntaxException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        } catch (e: IllegalStateException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        }
    }
}

fun parseOverrides(
    dataReducer: DataExpressionReducer<BasicNumber>,
    current: Map<String, DataExpression<BasicNumber>>,
    overrides: Map<String, String>,
): Map<String, DataExpression<BasicNumber>> = current.mapValues { entry ->
    when (val v = entry.value) {
        is QuantityExpression<*> -> overrides[entry.key]?.let {
            smartParseQuantityWithDefaultUnit(it, EUnitOf(v))
        } ?: entry.value

        is StringExpression -> overrides[entry.key]?.let {
            EStringLiteral(it)
        } ?: entry.value

        is EDefaultRecordOf -> {
            val dataSource = dataReducer.evalDataSource(v.dataSource)
            val schema = dataSource.schema
            val entries = schema.mapValues { schemaEntry ->
                when (val defaultValue = schemaEntry.value) {
                    is QuantityValue -> overrides[schemaEntry.key]?.let {
                        smartParseQuantityWithDefaultUnit(it, defaultValue.unit.toEUnitLiteral())
                    } ?: defaultValue.toEQuantityScale()

                    is StringValue -> overrides[schemaEntry.key]?.let {
                        EStringLiteral(it)
                    } ?: defaultValue.toEStringLiteral()

                    else -> throw EvaluatorException("datasource '${dataSource.config.name}': column '${
                        schemaEntry
                            .key
                    }': invalid default value")
                }
            }
            ERecord(entries)
        }

        else -> throw EvaluatorException("$v is not a supported data expression")
    }
}

fun prepareArguments(
    dataReducer: DataExpressionReducer<BasicNumber>,
    template: EProcessTemplate<BasicNumber>,
    request: Map<String, String>,
) = parseOverrides(dataReducer, template.params, request)
