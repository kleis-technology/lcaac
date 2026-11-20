package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.grammar.parseLcaFile
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.isRegularFile
import org.apache.commons.compress.archivers.examples.Expander
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream


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

/**
 * The symbol table where each global given with -G is described with its given value,
 * instead of being located at the definition that it replaces.
 */
fun <Q> SymbolTable<Q>.withGivenGlobals(globals: Map<String, String>): SymbolTable<Q> = copy(
    sourceMap = globals.entries.fold(sourceMap) { map, (name, value) ->
        map.describedAs(SourcePath.Global(name), "global '$name' = $value (given with -G)")
    }
)
