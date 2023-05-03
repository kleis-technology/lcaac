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

class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
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

    fun testParse_substance_shouldParseFields() {
        val subName = "co2"
        val type = SubstanceType.RESOURCE
        val compartment = "air"
        val subCompartment = "low pop"
        val file = parseFile(
                "hello", """
                substance $subName {
                    name = "carbon dioxide"
                    type = $type
                    compartment = "$compartment"
                    sub_compartment = "$subCompartment"
                    reference_unit = kg
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()

        // when
        val actual = symbolTable.getSubstanceCharacterization(
                name = subName,
                type = type,
                compartment = compartment,
                subCompartment = subCompartment,
        )!!.referenceExchange.substance

        // then
        TestCase.assertEquals("co2", actual.name)
        TestCase.assertEquals("carbon dioxide", actual.displayName)
        TestCase.assertEquals("air", actual.compartment)
        TestCase.assertEquals("low pop", actual.subCompartment)
        TestCase.assertEquals(EUnitRef("kg"), actual.referenceUnit)
    }

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

    fun testParse_whenDefineSameSubstanceTwice_shouldThrow() {
        // given
        val file = parseFile(
                "hello", """
                substance a {
                    name = "first"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg
                }
                substance a {
                    name = "second"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg 
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
                    "[a_compartment_Resource] are already bound",
                    e.message
            )
        }
    }

    fun testParse_whenDefineSameSubstanceDifferentSubCompartments_shouldNotThrow() {
        // given
        val file = parseFile(
                "hello", """
                substance a {
                    name = "first"
                    type = Resource
                    compartment = "compartment"
                    sub_compartment = "subCompartment"
                    reference_unit = kg
                }
                substance a {
                    name = "second"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg 
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
                sequenceOf(file)
        )

        // when/then
        try {
            parser.load()
        } catch (e: EvaluatorException) {
            fail("Threw: $e")
        }
    }

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

    fun testParse_unitExpression_div() {
        // given
        val subName = "a"
        val type = SubstanceType.RESOURCE
        val compartment = "compartment"
        val file = parseFile(
                "hello", """
            substance $subName {
                name = "a"
                type = $type
                compartment = "$compartment"
                reference_unit = x/y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
                sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstanceCharacterization(
                name = subName,
                type = type,
                compartment = compartment,
        )!!.referenceExchange.substance
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitDiv(
                EUnitRef("x"),
                EUnitRef("y"),
        )
        TestCase.assertEquals(expected, actual)
    }

    fun testParse_unitExpression_mul() {
        // given
        val subName = "a"
        val type = SubstanceType.RESOURCE
        val compartment = "compartment"
        val file = parseFile(
                "hello", """
            substance $subName {
                name = "a"
                type = $type
                compartment = "$compartment"
                reference_unit = x*y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
                sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstanceCharacterization(
                name = subName,
                type = type,
                compartment = compartment,
        )!!.referenceExchange.substance
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitMul(
                EUnitRef("x"),
                EUnitRef("y"),
        )
        TestCase.assertEquals(expected, actual)
    }

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

    fun testParse_substanceWithImpacts_shouldReturnASubstanceCharacterization() {
        // given
        val name = "phosphate"
        val type = SubstanceType.RESOURCE
        val compartment = "phosphate compartment"
        val subCompartment = "phosphate sub-compartment"
        val file = parseFile(
                "substances", """
            substance $name {
                name = "phosphate"
                type = $type
                compartment = "$compartment"
                sub_compartment = "$subCompartment"
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
        val actual = symbolTable.getSubstanceCharacterization(
                name = name,
                type = type,
                compartment = compartment,
                subCompartment = subCompartment
        )

        // then
        val expected = ESubstanceCharacterization(
                referenceExchange = EBioExchange(
                        EQuantityLiteral(1.0, EUnitRef("kg")),
                        ESubstanceSpec(
                                name = "phosphate",
                                type = SubstanceType.RESOURCE,
                                compartment = "phosphate compartment",
                                subCompartment = "phosphate sub-compartment",
                                referenceUnit = EUnitRef("kg"),
                        ),
                ),
                impacts = listOf(
                        EImpact(
                                EQuantityScale(1.0, EQuantityRef("kg")),
                                EIndicatorSpec("climate_change"),
                        )
                )
        )
        TestCase.assertEquals(expected, actual)
    }

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
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body).products[0]
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
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body).products[0]
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
