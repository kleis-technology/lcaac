package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.junit.Test

internal class LcaLexerText : LexerTestCase() {
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
                dataset "elecricity" { 
                    products {
                        - "nuclear" 1.3e10 kBq
                        - "power" 10 kg
                        - "plop"  1.3 ha
                    }
                }
                """,
            """
                WHITE_SPACE ('\n                ')
                LcaTokenType.dataset ('dataset')
                WHITE_SPACE (' ')
                LcaTokenType.string ('"elecricity"')
                WHITE_SPACE (' ')
                LcaTokenType.left-bracket ('{')
                WHITE_SPACE (' \n                    ')
                LcaTokenType.products ('products')
                WHITE_SPACE (' ')
                LcaTokenType.left-bracket ('{')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.list ('-')
                WHITE_SPACE (' ')
                LcaTokenType.string ('"nuclear"')
                WHITE_SPACE (' ')
                LcaTokenType.NUMBER ('1.3e10')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('kBq')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.list ('-')
                WHITE_SPACE (' ')
                LcaTokenType.string ('"power"')
                WHITE_SPACE (' ')
                LcaTokenType.NUMBER ('10')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('kg')
                WHITE_SPACE ('\n                        ')
                LcaTokenType.list ('-')
                WHITE_SPACE (' ')
                LcaTokenType.string ('"plop"')
                WHITE_SPACE ('  ')
                LcaTokenType.NUMBER ('1.3')
                WHITE_SPACE (' ')
                LcaTokenType.IDENTIFIER ('ha')
                WHITE_SPACE ('\n                    ')
                LcaTokenType.right-bracker ('}')
                WHITE_SPACE ('\n                ')
                LcaTokenType.right-bracker ('}')
                WHITE_SPACE ('\n                ')
            """.trimIndent()
        )
    }

    @Test
    fun testEmptyDataset()
    {
        doTest("""
            dataset "empty" { 
            }
        """.trimIndent(), """
            LcaTokenType.dataset ('dataset')
            WHITE_SPACE (' ')
            LcaTokenType.string ('"empty"')
            WHITE_SPACE (' ')
            LcaTokenType.left-bracket ('{')
            WHITE_SPACE (' \n')
            LcaTokenType.right-bracker ('}')
        """.trimIndent())
    }




    @Test
    fun testMetaProperties() {
        doTest("""
            dataset props {
                meta {
                    - test = "property value"
                }
            }
        """.trimIndent(), """
            LcaTokenType.dataset ('dataset')
            WHITE_SPACE (' ')
            LcaTokenType.IDENTIFIER ('props')
            WHITE_SPACE (' ')
            LcaTokenType.left-bracket ('{')
            WHITE_SPACE ('\n    ')
            LcaTokenType.meta ('meta')
            WHITE_SPACE (' ')
            LcaTokenType.left-bracket ('{')
            WHITE_SPACE ('\n        ')
            LcaTokenType.list ('-')
            WHITE_SPACE (' ')
            LcaTokenType.IDENTIFIER ('test')
            WHITE_SPACE (' ')
            BAD_CHARACTER ('=')
            WHITE_SPACE (' ')
            LcaTokenType.string ('"property value"')
            WHITE_SPACE ('\n    ')
            LcaTokenType.right-bracker ('}')
            WHITE_SPACE ('\n')
            LcaTokenType.right-bracker ('}')
        """.trimIndent())

    }

    @Test
    fun testEscapedCharacter() {
        doTest("""
            dataset "props" {
                meta {
                    - test: "property \"value\""
                }
            }
        """.trimIndent(), """
            LcaTokenType.dataset ('dataset')
            WHITE_SPACE (' ')
            LcaTokenType.string ('"props"')
            WHITE_SPACE (' ')
            LcaTokenType.left-bracket ('{')
            WHITE_SPACE ('\n    ')
            LcaTokenType.meta ('meta')
            WHITE_SPACE (' ')
            LcaTokenType.left-bracket ('{')
            WHITE_SPACE ('\n        ')
            LcaTokenType.list ('-')
            WHITE_SPACE (' ')
            LcaTokenType.IDENTIFIER ('test')
            LcaTokenType.separator (':')
            WHITE_SPACE (' ')
            LcaTokenType.string ('"property \"value\""')
            WHITE_SPACE ('\n    ')
            LcaTokenType.right-bracker ('}')
            WHITE_SPACE ('\n')
            LcaTokenType.right-bracker ('}')
        """.trimIndent());
    }
}
