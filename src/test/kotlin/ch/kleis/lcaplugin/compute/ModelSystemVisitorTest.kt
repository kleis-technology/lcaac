package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import tech.units.indriya.AbstractUnit.ONE
import tech.units.indriya.quantity.Quantities.getQuantity
import tech.units.indriya.unit.Units.KILOGRAM
import tech.units.indriya.unit.Units.LITRE
import javax.measure.MetricPrefix

class ModelSystemVisitorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testVisitProcess_shouldParseName() {
        // given
        val file = parseFile("hello", """
            process hello {
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.name, "hello")
    }

    @Test
    fun testVisitProcess_shouldParseNameWithQuote() {
        // given
        val file = parseFile("hello", """
            process "hello world" {
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello world")

        // then
        assertEquals(actual.name, "hello world")
    }

    @Test
    fun testVisitProcess_products() {
        // given
        val file = parseFile("hello", """
            process hello {
                products {
                    - carrot 1 kg
                }
                
                products {
                    - water 2 l
                }
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.products.size, 2)
        assertEquals(actual.products[0].flow.name, "carrot")
        assertEquals(actual.products[0].quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(actual.products[1].flow.name, "water")
        assertEquals(actual.products[1].quantity, getQuantity(2.0, LITRE))
    }

    @Test
    fun testVisitProcess_inputs() {
        // given
        val file = parseFile("hello", """
            process hello {
                inputs {
                    - carrot 1 kg
                }
                
                inputs {
                    - water 2 l
                }
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.inputs.size, 2)
        assertEquals(actual.inputs[0].flow.name, "carrot")
        assertEquals(actual.inputs[0].quantity, getQuantity(1.0, KILOGRAM))
        assertEquals(actual.inputs[1].flow.name, "water")
        assertEquals(actual.inputs[1].quantity, getQuantity(2.0, LITRE))
    }

    @Test
    fun testVisitProcess_emissions() {
        // given
        val file = parseFile("hello", """
            process hello {
                emissions {
                    - carrot, air 1 kg
                }
                
                emissions {
                    - water 2 l
                    - water, air, "low pop" 3 ml
                }
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.emissions.size, 3)
        assertEquals(actual.emissions[0].flow.substance, "carrot")
        assertEquals(actual.emissions[0].flow.compartment, "air")
        assertEquals(actual.emissions[0].flow.subcompartment, null)
        assertEquals(actual.emissions[0].quantity, getQuantity(1.0, KILOGRAM))

        assertEquals(actual.emissions[1].flow.substance, "water")
        assertEquals(actual.emissions[1].flow.compartment, null)
        assertEquals(actual.emissions[1].flow.subcompartment, null)
        assertEquals(actual.emissions[1].quantity, getQuantity(2.0, LITRE))

        assertEquals(actual.emissions[2].flow.substance, "water")
        assertEquals(actual.emissions[2].flow.compartment, "air")
        assertEquals(actual.emissions[2].flow.subcompartment, "low pop")
        assertEquals(actual.emissions[2].quantity, getQuantity(3.0, MetricPrefix.MILLI(LITRE)))
    }

    @Test
    fun testVisitProcess_resources() {
        // given
        val file = parseFile("hello", """
            process hello {
                resources {
                    - carrot, "air" 1 kg
                }
                
                resources {
                    - water 2 l
                    - water, air, "low pop" 3 ml
                }
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.resources.size, 3)
        assertEquals(actual.resources[0].flow.substance, "carrot")
        assertEquals(actual.resources[0].flow.compartment, "air")
        assertEquals(actual.resources[0].flow.subcompartment, null)
        assertEquals(actual.resources[0].quantity, getQuantity(1.0, KILOGRAM))

        assertEquals(actual.resources[1].flow.substance, "water")
        assertEquals(actual.resources[1].flow.compartment, null)
        assertEquals(actual.resources[1].flow.subcompartment, null)
        assertEquals(actual.resources[1].quantity, getQuantity(2.0, LITRE))

        assertEquals(actual.resources[2].flow.substance, "water")
        assertEquals(actual.resources[2].flow.compartment, "air")
        assertEquals(actual.resources[2].flow.subcompartment, "low pop")
        assertEquals(actual.resources[2].quantity, getQuantity(3.0, MetricPrefix.MILLI(LITRE)))
    }

    @Test
    fun testVisitProcess_example() {
        // given
        val file = parseFile("hello", """
            process hello {
                products {
                    - carrot 1 kg
                }
                
                inputs {
                    - water 3 ml
                }
                
                emissions {
                    - "co2" 15 g
                }
                
                inputs {
                    - "gamma" 4 Bq
                }
            }
        """.trimIndent())
        val visitor = ModelSystemVisitor()

        // when
        file.accept(visitor)
        val actual = visitor.getSystem().getProcess("hello")

        // then
        assertEquals(actual.products.size, 1)
        assertEquals(actual.inputs.size, 2)
        assertEquals(actual.emissions.size, 1)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
