package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class LcaParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
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
