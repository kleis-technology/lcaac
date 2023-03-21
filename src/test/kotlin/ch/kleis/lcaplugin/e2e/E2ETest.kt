package ch.kleis.lcaplugin.e2e

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class E2ETest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_operationPriority() {
        // given
        val file = parseFile(
            "hello", """
            process p {
                variables {
                    q = 2 m/kg
                }
                products {
                    1 kg out
                }
                inputs {
                    3 kg * q in
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(listOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.getElements().first()
                val input = result.controllablePorts.getElements().first()
                val cf = result.value(output, input)

                TestCase.assertEquals("out from p{}", output.name())
                TestCase.assertEquals(1.0, cf.output.quantity().amount)
                TestCase.assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.output.quantity().unit)

                TestCase.assertEquals("in", input.name())
                TestCase.assertEquals(6.0, cf.input.quantity().amount)
                TestCase.assertEquals(DimensionFixture.length.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    @Test
    fun test_twoInstancesSameTemplate_whenOneImplicit() {
        // given
        val file = parseFile(
            "hello", """
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * (1 kg/m2) co2
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(listOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                TestCase.assertEquals("office from office{}", output.name())
                TestCase.assertEquals(1.0, cf.output.quantity().amount)
                TestCase.assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                TestCase.assertEquals("co2", input.name())
                TestCase.assertEquals(3.0, cf.input.quantity().amount)
                TestCase.assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    @Test
    fun test_twoInstancesSameTemplate_whenExplicit() {
        // given
        val file = parseFile(
            "hello", """
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk from desk( size = 1 m2 )
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * (1 kg/m2) co2
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(listOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                TestCase.assertEquals("office from office{}", output.name())
                TestCase.assertEquals(1.0, cf.output.quantity().amount)
                TestCase.assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                TestCase.assertEquals("co2", input.name())
                TestCase.assertEquals(3.0, cf.input.quantity().amount)
                TestCase.assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    @Test
    fun test_manyInstancesSameTemplate() {
        // given
        val file = parseFile(
            "hello", """
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk
                    1 piece desk from desk( size = 1 m2 )
                    1 piece desk from desk( size = 1 m2, density = 1 kg/m2 )
                    1 piece desk from desk( size = 1 m2, density = 2 kg/m2 )
                    1 piece desk from desk( size = 2 m2, density = 2 kg/m2 )
                    1 piece desk from desk( size = 2 m2, density = 1 kg/m2 )
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                    density = 1 kg/m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * density co2
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(listOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                TestCase.assertEquals("office from office{}", output.name())
                TestCase.assertEquals(1.0, cf.output.quantity().amount)
                TestCase.assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                TestCase.assertEquals("co2", input.name())
                TestCase.assertEquals(13.0, cf.input.quantity().amount)
                TestCase.assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
