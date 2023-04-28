package ch.kleis.lcaplugin.language.parser

import arrow.optics.Every
import arrow.optics.dsl.index
import arrow.optics.typeclasses.Index
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testParse_shouldLoadUnitAlias() {
        // given
        val file = parseFile(
            "hello", """
            unit lbs {
                symbol = "lbs"
                alias_for = 2.2 kg
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = symbolTable.getUnit("lbs")
        val expect = EUnitAlias("lbs", EQuantityScale(2.2, EQuantityRef("kg")))
        assertEquals(expect, actual)
    }

    @Test
    fun testParse_shouldLoadPreludeUnits() {
        // given
        val file = parseFile(
            "hello", """
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()

        // then
        TestCase.assertEquals(Prelude.units, symbolTable.units)
    }

    @Test
    fun testParse_shouldLoadPreludeUnitQuantities() {
        // given
        val file = parseFile(
            "hello", """
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()

        // then
        TestCase.assertEquals(Prelude.unitQuantities, symbolTable.quantities)
    }

    @Test
    fun testParse_blockUnit_shouldDeclareUnitRefAndQuantityRef() {
        // given
        val file = parseFile(
            "hello", """
                unit foo {
                    symbol = "foo"
                    dimension = "foo"
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val symbolTable = parser.load()

        // when
        val unit = symbolTable.getUnit("foo") as EUnitLiteral
        val quantity = symbolTable.getQuantity("foo") as EQuantityLiteral

        // then
        TestCase.assertEquals(quantity.unit, unit)
        TestCase.assertEquals(quantity.amount, 1.0)
    }

    @Test
    fun testParse_referenceStartingWithUnderscore_shouldParse() {
        // given
        val file = parseFile(
            "hello", """
                variables {
                    _1kg = 1 kg
                }
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getPsiGlobalVariablesBlocks().first()
            .getGlobalAssignments().first().getQuantityRef().getUID()

        // then
        val expected = "_1kg"
        TestCase.assertEquals(expected, actual.name)
    }

    @Test
    fun testParse_processWithLandUse_shouldParse() {
        val file = parseFile(
            "hello", """
                process a {
                    products {
                        1 kg x
                    }
                    land_use {
                        1 kg lu
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val symbolTable = parser.load()

        // when
        val template = symbolTable.processTemplates["a"] as ProcessTemplateExpression
        val actual =
            (ProcessTemplateExpression.eProcessTemplate.body.biosphere compose
                    Every.list() compose EBioExchange.substance).firstOrNull(template)

        // then
        TestCase.assertEquals("lu", actual?.name)
    }

    @Test
    fun testParse_substance_shouldParseFields() {
        val file = parseFile(
            "hello", """
                substance co2 {
                    name = "carbon dioxide"
                    type = Resource
                    compartment = "air"
                    sub_compartment = "low pop"
                    reference_unit = kg
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()

        // when
        val actual = symbolTable.substances["co2"] as ESubstanceSpec

        // then
        TestCase.assertEquals("co2", actual.name)
        TestCase.assertEquals("carbon dioxide", actual.displayName)
        TestCase.assertEquals("air", actual.compartment)
        TestCase.assertEquals("low pop", actual.subcompartment)
        TestCase.assertEquals(EUnitRef("kg"), actual.referenceUnit)
    }

    @Test
    fun testParse_whenDefineProcessTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                process a {
                }
                process a {
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        try {
            parser.load()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            TestCase.assertEquals(
                "[a] are already bound",
                e.message
            )
        }
    }

    @Test
    fun testParse_whenDefineProductTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                process a {
                    products {
                        1 kg x
                    }
                }
                process b {
                    products {
                        1 kg x
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        try {
            parser.load()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            TestCase.assertEquals(
                "x is already bound",
                e.message
            )
        }
    }

    @Test
    fun testParse_withoutPackage_thenDefaultPackageName() {
        // given
        val file = parseFile(
            "hello", """
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getPackageName()

        // then
        val expected = "default"
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_whenDefineGlobalVariableTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                variables {
                    x = 1 kg
                    x = 3 l
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        try {
            parser.load()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            TestCase.assertEquals("[x] are already bound", e.message)
        }
    }

    @Test
    fun testParse_whenDefineSubstanceTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                substance a {
                    name = "first"
                    type = Resource
                    compartment = "first compartment"
                    reference_unit = kg
                }
                substance a {
                    name = "second"
                    type = Resource
                    compartment = "second compartment"
                    reference_unit = l
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        try {
            parser.load()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            TestCase.assertEquals(
                "[a] are already bound",
                e.message
            )
        }
    }

    @Test
    fun testParse_withPackage_shouldReturnGivenPackageName() {
        // given
        val file = parseFile(
            "hello", """
                package a.b.c
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getPackageName()

        // then
        val expected = "a.b.c"
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_simpleProcess() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                products {
                    1 kg carrot
                }
                inputs {
                    10 l water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getTemplate("a")!!

        // then
        val preludeSymbolTable = SymbolTable(
            units = Prelude.units,
            quantities = Prelude.unitQuantities,
        )
        val expected = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            EProcess(
                name = "a",
                products = listOf(
                    ETechnoExchange(
                        EQuantityScale(1.0, EQuantityRef("kg")),
                        EProductSpec(
                            "carrot",
                            EUnitClosure(
                                preludeSymbolTable, EUnitOf(
                                    EQuantityScale(1.0, EQuantityRef("kg")),
                                )
                            )
                        ),
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        EQuantityScale(10.0, EQuantityRef("l")),
                        EProductSpec("water"),
                    ),
                ),
                biosphere = emptyList(),
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_unitExpression_div() {
        // given
        val file = parseFile(
            "hello", """
            substance a {
                name = "a"
                type = Resource
                compartment = "compartment"
                reference_unit = x/y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstance("a")!!
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitDiv(
            EUnitRef("x"),
            EUnitRef("y"),
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_unitExpression_mul() {
        // given
        val file = parseFile(
            "hello", """
            substance a {
                name = "a"
                type = Resource
                compartment = "compartment"
                reference_unit = x*y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstance("a")!!
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitMul(
            EUnitRef("x"),
            EUnitRef("y"),
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_quantityExpression_div() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                inputs {
                    10 x / (20 y) water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
                ProcessTemplateExpression.eProcessTemplate.body.inputs.index(Index.list(), 0) compose
                        ETechnoExchange.quantity
                ).getOrNull(template)!!

        // then
        val expected = EQuantityDiv(
            EQuantityScale(10.0, EQuantityRef("x")),
            EQuantityScale(20.0, EQuantityRef("y"))
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_quantityExpression_mul() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                inputs {
                    10 x * (20 y) water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
                ProcessTemplateExpression.eProcessTemplate.body.inputs.index(Index.list(), 0) compose
                        ETechnoExchange.quantity
                ).getOrNull(template)!!

        // then
        val expected = EQuantityMul(
            EQuantityScale(10.0, EQuantityRef("x")),
            EQuantityScale(20.0, EQuantityRef("y"))
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_withConstrainedProduct() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                products {
                    1 kg carrot
                }
                inputs {
                    10 l water from water_proc(x = 3 l)
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val expression = symbolTable.getTemplate("a")!!
        val actual =
            ProcessTemplateExpression.eProcessTemplate.body.inputs.getAll(expression).flatten()

        // then
        val expected = listOf(
            ETechnoExchange(
                EQuantityScale(10.0, EQuantityRef("l")),
                EProductSpec(
                    "water",
                    fromProcessRef = FromProcessRef(
                        "water_proc",
                        mapOf("x" to EQuantityScale(3.0, EQuantityRef("l"))),
                    ),
                )
            ),
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_substanceWithImpacts_shouldReturnASubstanceCharacterization() {
        // given
        val file = parseFile(
            "substances", """
            substance phosphate {
                name = "phosphate"
                type = Resource
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
                
                impacts {
                    1 kg climate_change
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getSubstanceCharacterization("phosphate")

        // then
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EQuantityLiteral(1.0, EUnitRef("kg")),
                ESubstanceSpec(
                    name = "phosphate",
                    type = SubstanceType.RESOURCE,
                    compartment = "phosphate compartment",
                    subcompartment = "phosphate sub-compartment",
                    referenceUnit = EUnitRef("kg"),
                ),
            ),
            impacts = listOf(
                EImpact(
                    EQuantityScale(1.0, EQuantityRef("kg")),
                    EIndicatorRef("climate_change"),
                )
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_productWithoutAllocation_should_return_100percent_allocation() {
        // given
        val file = parseFile(
            "carrot", """
            process carrot {
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body as EProcess).products[0]
        val preludeSymbolTable = SymbolTable(
            units = Prelude.units,
            quantities = Prelude.unitQuantities,
        )
        val expect = ETechnoExchange(
            EQuantityScale(1.0, EQuantityRef("kg")),
            EProductSpec(
                "carrot",
                EUnitClosure(preludeSymbolTable, EUnitOf(EQuantityScale(1.0, EQuantityRef("kg"))))
            ),
            EQuantityLiteral(100.0, EUnitLiteral("percent", 0.01, Dimension.None))
        )
        assertEquals(expect, actual)
    }

    @Test
    fun testParse_productWithAllocation() {
        // given
        val file = parseFile(
            "carrot", """
            process carrot {
                products {
                    1 kg carrot allocate 10 percent
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body as EProcess).products[0]
        val preludeSymbolTable = SymbolTable(
            units = Prelude.units,
            quantities = Prelude.unitQuantities,
        )
        val expect = ETechnoExchange(
            EQuantityScale(1.0, EQuantityRef("kg")),
            EProductSpec(
                "carrot",
                EUnitClosure(preludeSymbolTable, EUnitOf(EQuantityScale(1.0, EQuantityRef("kg"))))
            ),
            EQuantityScale(10.0, EQuantityRef("percent"))
        )
        assertEquals(expect, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
