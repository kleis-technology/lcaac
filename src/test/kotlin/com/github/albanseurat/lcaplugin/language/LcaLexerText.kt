package com.github.albanseurat.lcaplugin.language

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.junit.Test

class LcaLexerText : LexerTestCase() {
    override fun createLexer(): Lexer {
        return LcaLexerAdapter();
    }

    override fun getDirPath(): String? {
        return null;
    }

    @Test
    fun testSimpleDataset() {

        doTest(
            """
                dataset elecricity { 
                    products {
                        - nuclear 1.3e10 kBq
                        - power 10 kg
                        - plop  1.3 ha
                    }
                }
                """,
            """
                WHITE_SPACE ('\n                ')
                LcaTokenType.dataset ('dataset')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('elecricity')
                WHITE_SPACE (' ')
                LcaTokenType.{ ('{')
                WHITE_SPACE (' \n                    ')
                LcaTokenType.products ('products')
                WHITE_SPACE (' ')
                LcaTokenType.{ ('{')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.- ('-')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('nuclear')
                WHITE_SPACE (' ')
                LcaTokenType.NUMBER ('1.3e10')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('kBq')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.- ('-')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('power')
                WHITE_SPACE (' ')
                LcaTokenType.NUMBER ('10')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('kg')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.- ('-')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('plop')
                WHITE_SPACE ('  ')
                LcaTokenType.NUMBER ('1.3')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('ha')
                WHITE_SPACE ('\n                    ')
                LcaTokenType.} ('}')
                WHITE_SPACE ('\n                ')
                LcaTokenType.} ('}')
                WHITE_SPACE ('\n                ')
            """.trimIndent()
        )
    }
}