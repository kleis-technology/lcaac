package ch.kleis.lcaac.cli

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile

class EvalCommand : CliktCommand() {
    val query: String by option("-q", "--query", help="query").required()
    val root: String by option("-r", "--root", help="root folder").default(".")

    private fun lcaFile(inputStream: InputStream): LcaLangParser.LcaFileContext {
        val lexer = LcaLangLexer(CharStreams.fromStream(inputStream))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        return parser.lcaFile()
    }

    private fun productSpec(inputStream: InputStream): EProductSpec<BasicNumber> {
        val lexer = LcaLangLexer(CharStreams.fromStream(inputStream))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val ctx = parser.inputProductSpec()
        return CoreMapper(BasicOperations).inputProductSpec(ctx)
    }

    override fun run() {
        val files = Files.walk(Paths.get(root))
            .filter { it.isRegularFile() }
            .filter { it.toString().endsWith(".lca") }
            .map { lcaFile(it.toFile().inputStream()) }
            .toList()
            .asSequence()
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(files, listOf(LoaderOption.WITH_PRELUDE))
        val evaluator = Evaluator(symbolTable, BasicOperations)
        val request = productSpec(query.byteInputStream())
        val trace = evaluator.trace(setOf(request))
        val entryPoint = trace.getEntryPoint()
        val system = trace.getSystemValue()
        val program = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val analysis = program.run()


        val rows = entryPoint.products.map { it.product }
            .flatMap { product ->
                val indicators = analysis.getIndicators()
                indicators.map { indicator ->
                    val impact =
                        analysis.getUnitaryImpacts(product)[indicator] ?: QuantityValueOperations(BasicOperations).pure(
                            0.0
                        )
                    listOf(
                        product.name,
                        "1.0",
                        product.referenceUnit.toString(),
                        impact.amount.toString(),
                        impact.unit.toString(),
                        indicator.name,
                    ).joinToString(",")
                }
            }
        rows.forEach { println(it) }
    }
}

fun main(args: Array<String>) = EvalCommand().main(args)
