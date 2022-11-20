package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import tech.units.indriya.AbstractUnit.ONE
import tech.units.indriya.quantity.Quantities.getQuantity
import tech.units.indriya.unit.Units.KILOGRAM

class ModelMethodVisitorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testVisitSubstance() {
        // given
        val file = parseFile("hello", """
            substance hello {
                type: emissions
                unit: kg
                
                factors : ef31 {
                    - "climate change" 3.0 kg
                }
            }
        """.trimIndent())
        val visitor = ModelMethodVisitor()

        // when
        file.accept(visitor)
        val method = visitor.getMethodMap()["ef31"] ?: throw IllegalStateException()

        //then
        assertEquals(method[0].output.flow.getUniqueId(), "hello")
        assertEquals(method[0].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[0].input.flow.getUniqueId(), "climate change")
        assertEquals(method[0].input.quantity, getQuantity(3.0, ONE))
    }

    @Test
    fun testVisitSubstance_multipleSubstances() {
        // given
        val file = parseFile("hello", """
            substance hello {
                type: emissions
                unit: kg
                
                factors : ef31 {
                    - "climate change" 3.0
                }
            }
            
            substance bar {
                type: emissions
                unit: kg
                
                factors : ef31 {
                    - "climate change" 4.0
                }
            }
        """.trimIndent())
        val visitor = ModelMethodVisitor()

        // when
        file.accept(visitor)
        val method = visitor.getMethodMap()["ef31"] ?: throw IllegalStateException()

        //then
        assertEquals(method[0].output.flow.getUniqueId(), "hello")
        assertEquals(method[0].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[0].input.flow.getUniqueId(), "climate change")
        assertEquals(method[0].input.quantity, getQuantity(3.0, ONE))

        assertEquals(method[1].output.flow.getUniqueId(), "bar")
        assertEquals(method[1].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[0].input.flow.getUniqueId(), "climate change")
        assertEquals(method[1].input.quantity, getQuantity(4.0, ONE))
    }

    @Test
    fun testVisitSubstance_multipleIndicators() {
        // given
        val file = parseFile("hello", """
            substance hello {
                type: emissions
                unit: kg
                
                factors : ef31 {
                    - "climate change" 3.0
                    - eutrophication 3.0
                }
            }
            
            substance bar {
                type: emissions
                unit: kg
                
                factors : ef31 {
                    - "climate change" 4.0
                    - eutrophication 7.0
                }
            }
        """.trimIndent())
        val visitor = ModelMethodVisitor()

        // when
        file.accept(visitor)
        val method = visitor.getMethodMap()["ef31"] ?: throw IllegalStateException()

        //then
        assertEquals(method[0].output.flow.getUniqueId(), "hello")
        assertEquals(method[0].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[0].input.flow.getUniqueId(), "climate change")
        assertEquals(method[0].input.quantity, getQuantity(3.0, ONE))

        assertEquals(method[1].output.flow.getUniqueId(), "hello")
        assertEquals(method[1].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[1].input.flow.getUniqueId(), "eutrophication")
        assertEquals(method[1].input.quantity, getQuantity(3.0, ONE))

        assertEquals(method[2].output.flow.getUniqueId(), "bar")
        assertEquals(method[2].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[0].input.flow.getUniqueId(), "climate change")
        assertEquals(method[2].input.quantity, getQuantity(4.0, ONE))

        assertEquals(method[3].output.flow.getUniqueId(), "bar")
        assertEquals(method[3].output.quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(method[3].input.flow.getUniqueId(), "eutrophication")
        assertEquals(method[3].input.quantity, getQuantity(7.0, ONE))
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
