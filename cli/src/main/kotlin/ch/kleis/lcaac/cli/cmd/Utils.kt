package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.InputStream
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
