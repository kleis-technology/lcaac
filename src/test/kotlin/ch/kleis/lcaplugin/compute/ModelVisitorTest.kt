package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import tech.units.indriya.quantity.Quantities.getQuantity
import tech.units.indriya.unit.Units.KILOGRAM
import tech.units.indriya.unit.Units.LITRE

class ModelVisitorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testVisitProcess_shouldParseName() {
        // given
        val file = parseFile("hello", """
            process "hello" {
            }
        """.trimIndent())
        val visitor = ModelVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.get()

        // then
        assertEquals(actual[0].name, "hello")
    }

    @Test
    fun testVisitProcess_shouldProducts() {
        // given
        val file = parseFile("hello", """
            process "hello" {
                products {
                    - "carrot" 1 kg
                }
                
                products {
                    - "water" 2 l
                }
            }
        """.trimIndent())
        val visitor = ModelVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.get()[0]

        // then
        assertEquals(actual.products.size, 2)
        assertEquals(actual.products[0].flow, "carrot")
        assertEquals(actual.products[0].quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(actual.products[1].flow, "water")
        assertEquals(actual.products[1].quantity, getQuantity(2.0, LITRE))
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
