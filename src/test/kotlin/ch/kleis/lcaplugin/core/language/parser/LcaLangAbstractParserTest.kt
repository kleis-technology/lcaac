package ch.kleis.lcaplugin.core.language.parser

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testParse() {
        // given
        val file = parseFile(
            "hello", """
            package hello
            
            process a {
                1 kg carrot
                
                inputs {
                    10 l water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }

        // when
        val (pkg, _) = parser.collect("hello")
        val actual = pkg.definitions["a"]!!

        // then
        val expected = EProcess(
            listOf(
                EExchange(EQuantity(1.0, EVar("kg")), EVar("carrot")),
                EBlock(
                    listOf(
                        EExchange(ENeg(EQuantity(10.0, EVar("l"))), EVar("water")),
                    )
                )
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_localProducts() {
        // given
        val file = parseFile(
            "hello", """
            package hello
            
            unit kg {
                symbol = "kg"
                scale = 1.0
                dimension = "mass"
            }
            
            system main {
                let {
                    p = product {
                        name = "p"
                        reference_unit = kg
                    }
                }
                
                process {
                    1 kg p
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }

        // when
        val (pkg, _) = parser.collect("hello")
        val actual = pkg.definitions["main"]!!

        // then
        val expected = EProcess(
            listOf(
                EExchange(EQuantity(1.0, EVar("kg")), EVar("carrot")),
                EBlock(
                    listOf(
                        EExchange(ENeg(EQuantity(10.0, EVar("l"))), EVar("water")),
                    )
                )
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
