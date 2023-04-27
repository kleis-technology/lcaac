package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaFileCollectorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testCollect_shouldCollectAllFilesResolved() {
        // given
        val fa = parseFile(
            "fa", """
            package ch.kleis.pkg_a
            
            import ch.kleis.pkg_b
            
            process a {
                products {
                    1 kg a
                }
                emissions {
                    1 kg b
                }
            }
        """.trimIndent()
        ) as LcaFile
        val fb = parseFile(
            "fb", """
            package ch.kleis.pkg_b
            
            substance b {
                name = "b"
                compartment = "compartment"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        val collector = LcaFileCollector {
            fb
        }

        // when
        val actual = collector.collect(fa).toList()

        // then
        assertEquals(listOf(fa, fb), actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
