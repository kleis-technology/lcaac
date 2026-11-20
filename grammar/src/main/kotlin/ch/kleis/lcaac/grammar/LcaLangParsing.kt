package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.*
import java.nio.file.Files
import java.nio.file.Path

data class SyntaxError(
    val source: String,
    val line: Int,
    val column: Int,
    val message: String,
) {
    override fun toString(): String = "$source:$line:$column: $message"
}

class SyntaxException(val errors: List<SyntaxError>) : Exception(errors.joinToString("\n"))

/**
 * Parses an LCA file, failing with a [SyntaxException] that locates every syntax error
 * instead of printing them on the console and recovering. [sourceName] names the file in the
 * locations of errors.
 */
fun parseLcaFile(path: Path, sourceName: String = path.toString()): LcaLangParser.LcaFileContext =
    parse(Files.newBufferedReader(path).use { CharStreams.fromReader(it, sourceName) }) { it.lcaFile() }

/**
 * Parses LCA source code given as text, e.g. a model embedded in a test.
 */
fun parseLcaText(text: String, sourceName: String = "<text>"): LcaLangParser.LcaFileContext =
    parse(CharStreams.fromString(text, sourceName)) { it.lcaFile() }

/**
 * Parses a whole data expression, e.g. a value given on the command line.
 * Trailing input that is not part of the expression is a syntax error.
 */
fun parseDataExpression(text: String, sourceName: String = "<expression>"): LcaLangParser.DataExpressionContext =
    parse(CharStreams.fromString(text, sourceName)) { it.dataExpression() }

private fun <T> parse(input: CharStream, rule: (LcaLangParser) -> T): T {
    val listener = CollectingErrorListener(input.sourceName)
    val lexer = LcaLangLexer(input)
    lexer.removeErrorListeners()
    lexer.addErrorListener(listener)
    val parser = LcaLangParser(CommonTokenStream(lexer))
    parser.removeErrorListeners()
    parser.addErrorListener(listener)

    val result = rule(parser)
    val next = parser.currentToken
    if (listener.errors.isEmpty() && next.type != Token.EOF) {
        listener.add(next.line, next.charPositionInLine, "unexpected input '${next.text}'")
    }
    if (listener.errors.isNotEmpty()) throw SyntaxException(listener.errors)
    return result
}

private class CollectingErrorListener(private val source: String) : BaseErrorListener() {
    val errors = mutableListOf<SyntaxError>()

    fun add(line: Int, charPositionInLine: Int, message: String) {
        // ANTLR columns start at 0, editors count from 1
        errors.add(SyntaxError(source, line, charPositionInLine + 1, message))
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?,
    ) = add(line, charPositionInLine, msg)
}
