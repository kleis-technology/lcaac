package ch.kleis.lcaplugin.language.psi.factory

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaUIDFactoryTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun test_createUid() {
        // when
        val actual = LcaUIDFactory { content ->
            parseFile("create_uid", content) as LcaFile
        }.createUid("foo")

        // then
        assertEquals("foo", actual.name)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
