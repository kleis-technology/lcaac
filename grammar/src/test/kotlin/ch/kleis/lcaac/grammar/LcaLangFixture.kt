package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class LcaLangFixture {
    companion object {
        @Deprecated("use parser instead")
        fun lcaFile(content: String): LcaLangParser.LcaFileContext {
            val lexer = LcaLangLexer(CharStreams.fromString(content))
            val tokens = CommonTokenStream(lexer)
            val parser = LcaLangParser(tokens)
            return parser.lcaFile()
        }

        @Deprecated("use parser instead")
        fun test(content: String): LcaLangParser.TestDefinitionContext {
            val lexer = LcaLangLexer(CharStreams.fromString(content))
            val tokens = CommonTokenStream(lexer)
            val parser = LcaLangParser(tokens)
            return parser.testDefinition()
        }

        fun parser(content: String): LcaLangParser {
            val lexer = LcaLangLexer(CharStreams.fromString(content))
            val tokens = CommonTokenStream(lexer)
            return LcaLangParser(tokens)
        }
    }
}
