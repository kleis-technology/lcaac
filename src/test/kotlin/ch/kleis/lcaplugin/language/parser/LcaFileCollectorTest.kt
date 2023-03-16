package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaFileCollectorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_collect() {
        // given
        val fa = parseFile(
            "fa", """
            package ch.kleis.pkg_a
            
            import ch.kleis.pkg_b
            
            process a {
                products {
                    1 kg a
                }
            }
        """.trimIndent()
        ) as LcaFile
        val fb = parseFile(
            "fb", """
            package ch.kleis.pkg_b
            
            process b {
                products {
                    1 kg b
                }
            }
        """.trimIndent()
        ) as LcaFile
        val fc = parseFile(
            "fc", """
            package ch.kleis.pkg_c
            
            process c {
                products {
                    1 kg c
                }
            }
        """.trimIndent()
        ) as LcaFile
        val collector = LcaFileCollector(listOf(fa, fb, fc))

        // when
        val actual = collector.collect("ch.kleis.pkg_a")

        // then
        assertEquals(listOf(fa, fb), actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
