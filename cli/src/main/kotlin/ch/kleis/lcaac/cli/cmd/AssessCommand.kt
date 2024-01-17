package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvProcessor
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.CsvResultWriter
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.isRegularFile

class AssessCommand : CliktCommand(name = "assess", help = "Returns the unitary impacts of a process in CSV format") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label").associate()
    val root: File by option("-r", "--root").file(canBeFile = false).default(File(".")).help("Root folder")
    val data: File? by option("-d", "--data").file(canBeDir = false).help("CSV file with parameter values")

    override fun run() {
        val symbolTable = loadSymbolTable()
        val processor = CsvProcessor(symbolTable)
        val iterator = loadRequests()
        val writer = CsvResultWriter()
        var first = true
        while (iterator.hasNext()) {
            val request = iterator.next()
            val results = processor.process(request)
            for (it in results) {
                if (first) {
                    echo(writer.header(it), trailingNewline = false)
                    first = false
                }
                echo(writer.row(it), trailingNewline = false)
            }
        }
    }

    private fun loadRequests(): Iterator<CsvRequest> {
        return data?.let { loadRequestsFrom(it) }
            ?: listOf(CsvRequest(name, labels, emptyMap(), emptyList())).iterator()
    }

    private fun loadRequestsFrom(file: File): Iterator<CsvRequest> {
        val reader = CsvRequestReader(name, labels, file.inputStream())
        return reader.iterator()
    }

    private fun loadSymbolTable(): SymbolTable<BasicNumber> {
        val files = Files.walk(root.toPath())
            .filter { it.isRegularFile() }
            .filter { it.toString().endsWith(".lca") }
            .map { lcaFile(it.toFile().inputStream()) }
            .toList()
            .asSequence()
        val loader = Loader(BasicOperations)
        return loader.load(files, listOf(LoaderOption.WITH_PRELUDE))
    }

    private fun lcaFile(inputStream: InputStream): LcaLangParser.LcaFileContext {
        val lexer = LcaLangLexer(CharStreams.fromStream(inputStream))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        return parser.lcaFile()
    }

}
