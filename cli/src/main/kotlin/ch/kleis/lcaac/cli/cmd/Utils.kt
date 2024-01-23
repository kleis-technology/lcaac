package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.InputStream
import java.lang.Double.parseDouble
import java.nio.file.Files
import kotlin.io.path.isRegularFile

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

