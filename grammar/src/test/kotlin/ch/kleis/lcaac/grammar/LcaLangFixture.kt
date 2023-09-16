package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class LcaLangFixture {
    companion object {
        fun lcaFile(content: String): LcaLangParser.LcaFileContext {
            val lexer = LcaLangLexer(CharStreams.fromString(content))
            val tokens = CommonTokenStream(lexer)
            val parser = LcaLangParser(tokens)
            return parser.lcaFile()
        }
    }
}
