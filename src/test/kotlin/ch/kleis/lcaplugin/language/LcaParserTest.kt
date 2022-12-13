package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class LcaParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testGlobalParameters() {
        // given
        val file = parseFile("hello", """
            package test
            
            parameters {
                - A : 1.0
                - B : ${'$'}{2 * A}
            }
        """.trimIndent()) as LcaFile

        // when
        val actual = file.getGlobalParameters().toList()

        // then
        TestCase.assertEquals(actual.size, 2)
        TestCase.assertEquals(actual[0].name, "A")
        TestCase.assertEquals(actual[0].getExpression().getContent(), "1.0")
        TestCase.assertEquals(actual[1].name, "B")
        TestCase.assertEquals(actual[1].getExpression().getContent(), "2 * A")
    }

    @Test
    fun testImportWildcard() {
        // given
        val file = parseFile("hello", """
            package test
            
            import ef31.*
            
        """.trimIndent()) as LcaFile

        // when
        val actual = file.getImports().first().isWildcard()

        // then
        TestCase.assertEquals(actual, true)
    }

    @Test
    fun testImportWithoutWildcard() {
        // given
        val file = parseFile("hello", """
            package test
            
            import ef31.substance
            
        """.trimIndent()) as LcaFile

        // when
        val actual = file.getImports().first().isWildcard()

        // then
        TestCase.assertEquals(actual, false)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
