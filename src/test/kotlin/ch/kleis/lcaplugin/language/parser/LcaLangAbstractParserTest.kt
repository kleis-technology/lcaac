package ch.kleis.lcaplugin.language.parser

import arrow.optics.Every
import arrow.optics.dsl.index
import arrow.optics.typeclasses.Index
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    private val ops = BasicOperations

    @Test
    fun test_stringVariables() {
        // given
        val file = parseFile(
            "hello", """
                variables {
                    x = "hello"
                }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when
        val actual = parser.load().getData("x")

        // then
        val expected = EStringLiteral<BasicNumber>("hello")
        assertEquals(expected, actual)
    }

    @Test
    fun test_shouldMapLabels() {
        // given
        val file = parseFile(
            "hello", """
                process p {
                    labels {
                        xyz = "abc"
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when
        val actual = parser.load().getTemplate(
            "p", mapOf("xyz" to "abc"),
        )!!.body.labels

        // then
        val expected = mapOf("xyz" to EStringLiteral<BasicNumber>("abc"))
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_shouldPreventDefiningTwoReferenceUnitsForTheSameDimension() {
        // given
        val file = parseFile(
            "hello", """
            unit foo1 {
                symbol = "foo1"
                dimension = "foo"
            }
            
            unit foo2 {
                symbol = "foo2"
                dimension = "foo"
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate reference units for dimensions [foo]", e.message)
    }

    @Test
    fun testParse_shouldPreventDefiningReferenceUnitForDimensionInPrelude() {
        // TODO Review ça
        // given
        val file = parseFile(
            "hello", """
            unit foo1 {
                symbol = "foo1"
                dimension = "mass"
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate reference units for dimensions [mass]", e.message)
    }

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
            sequenceOf(file),
            ops,
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.getData("lbs")
        val expect = EUnitAlias("lbs", EQuantityScale(ops.pure(2.2), EDataRef("kg")))
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
            sequenceOf(file),
            ops,
        )


        // when
        val symbolTable = parser.load()

        // then
        Prelude.units<BasicNumber>().getValues().onEach {
            assertNotNull(symbolTable.getData(it.toString()))
        }
    }

    @Test
    fun testParse_blockUnit_shouldDeclareQuantityRef() {
        // given
        val file = parseFile(
            "hello", """
                unit fooUnitName {
                    symbol = "fooSymbol"
                    dimension = "fooDimension"
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        val symbolTable = parser.load()
        val unit = "fooSymbol"

        // when
        val quantity = symbolTable.getData("fooUnitName") as EUnitLiteral<BasicNumber>

        // then
        assertEquals(quantity.symbol.toString(), unit)
        assertEquals(quantity.scale, 1.0)
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
        val actual = file.getBlocksOfGlobalVariables().first()
            .globalAssignmentList.first().getDataRef().getUID()

        // then
        val expected = "_1kg"
        assertEquals(expected, actual.name)
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
            sequenceOf(file),
            ops,
        )
        val symbolTable = parser.load()

        // when
        val template = symbolTable.getTemplate("a") as ProcessTemplateExpression<BasicNumber>
        val actual =
            (ProcessTemplateExpression.eProcessTemplate<BasicNumber>().body().biosphere() compose
                    Every.list() compose EBioExchange.substance()).firstOrNull(template)

        // then
        assertEquals("lu", actual?.name)
    }

    @Test
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
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()

        // when
        val actual = symbolTable.getSubstanceCharacterization(
            name = subName,
            type = type,
            compartment = compartment,
            subCompartment = subCompartment,
        )!!.referenceExchange.substance

        // then
        assertEquals("co2", actual.name)
        assertEquals("carbon dioxide", actual.displayName)
        assertEquals("air", actual.compartment)
        assertEquals("low pop", actual.subCompartment)
        assertEquals(EUnitOf<BasicNumber>(EDataRef("kg")), actual.referenceUnit)
    }

    @Test
    fun testParse_whenDefineUnitTwice_shouldThrow() {
        val file = parseFile(
            "testParse_whenDefineUnitTwice_shouldThrow.lca",
            """
            unit foo {
                symbol = "foo"
                alias_for = 1 u
            }
            unit foo {
                symbol = "foo"
                alias_for = 10 u
            }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate global variable [foo] defined", e.message)
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
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate process [a] defined", e.message)
    }

    @Test
    fun testParse_whenDefineProductTwice_shouldIndexBothProcesses() {
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
            sequenceOf(file),
            ops,
        )

        // when
        val actual = parser.load().getAllTemplatesByProductName("x")

        // then
        assertEquals(listOf("a", "b"), actual.map { it.body.name })
        assertEquals(listOf("x", "x"), actual.flatMap { it.body.products }.map { it.product.name })
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
        assertEquals(expected, actual)
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
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate global variable [x] defined", e.message)
    }

    @Test
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
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate substance [a_compartment_Resource] defined", e.message)
    }

    @Test
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
            sequenceOf(file),
            ops,
        )

        // when, then should not throw
        parser.load()
    }

    @Test
    fun testParse_whenDefineUnitAndVariableWithSameName_shouldThrow() {
        // given
        // unit p from prelude
        val file = parseFile(
            "hello", """
                process p {
                    variables {
                        p = 1 kg
                    }
                    products {
                        1 kg productName
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Conflict between local variable(s) [p] and a global definition.", e.message)
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
        assertEquals(expected, actual)
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
            sequenceOf(createFile("unit.lca", UnitFixture.basicUnits) as LcaFile, file),
            ops
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getTemplate("a")!!

        // then
        val unitsSymbolTable = SymbolTable(
            data = symbolTable.data
        )
        val expected = EProcessTemplate(
            body = EProcess(
                name = "a",
                products = listOf(
                    ETechnoExchange(
                        EQuantityScale(ops.pure(1.0), EDataRef("kg")),
                        EProductSpec(
                            "carrot",
                            EUnitOf(
                                EQuantityClosure(
                                    unitsSymbolTable,
                                    EQuantityScale(ops.pure(1.0), EDataRef("kg")),
                                )
                            ),
                        ),
                        EQuantityScale(ops.pure(100.0), EDataRef("percent"))
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        EQuantityScale(ops.pure(10.0), EDataRef("l")),
                        EProductSpec("water"),
                    ),
                ),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
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
            sequenceOf(file),
            ops,
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
        val expected = EUnitOf<BasicNumber>(
            EQuantityDiv(
                EDataRef("x"),
                EDataRef("y"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
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
            sequenceOf(file),
            ops,
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
        val expected = EUnitOf<BasicNumber>(
            EQuantityMul(
                EDataRef("x"),
                EDataRef("y"),
            )
        )
        assertEquals(expected, actual)
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
            sequenceOf(file),
            ops,
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
                ProcessTemplateExpression.eProcessTemplate<BasicNumber>().body().inputs().index(Index.list(), 0) compose
                        ETechnoExchange.quantity()
                ).getOrNull(template)!!

        // then
        val expected = EQuantityDiv(
            EQuantityScale(ops.pure(10.0), EDataRef("x")),
            EQuantityScale(ops.pure(20.0), EDataRef("y"))
        )
        assertEquals(expected, actual)
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
            sequenceOf(file),
            ops,
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
                ProcessTemplateExpression.eProcessTemplate<BasicNumber>().body().inputs().index(Index.list(), 0) compose
                        ETechnoExchange.quantity()
                ).getOrNull(template)!!

        // then
        val expected = EQuantityMul(
            EQuantityScale(ops.pure(10.0), EDataRef("x")),
            EQuantityScale(ops.pure(20.0), EDataRef("y"))
        )
        assertEquals(expected, actual)
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
            sequenceOf(file),
            ops,
        )

        // when
        val symbolTable = parser.load()
        val expression = symbolTable.getTemplate("a")!!
        val actual =
            ProcessTemplateExpression.eProcessTemplate<BasicNumber>().body().inputs().getAll(expression).flatten()

        // then
        val expected = listOf(
            ETechnoExchange(
                EQuantityScale(ops.pure(10.0), EDataRef("l")),
                EProductSpec(
                    "water",
                    fromProcess = FromProcess(
                        "water_proc",
                        MatchLabels(emptyMap()),
                        mapOf("x" to EQuantityScale(ops.pure(3.0), EDataRef("l"))),
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
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
            sequenceOf(file),
            ops,
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
                EDataRef("kg"),
                ESubstanceSpec(
                    name = "phosphate",
                    type = SubstanceType.RESOURCE,
                    compartment = "phosphate compartment",
                    subCompartment = "phosphate sub-compartment",
                    referenceUnit = EUnitOf(EDataRef("kg")),
                ),
            ),
            impacts = listOf(
                EImpact(
                    EQuantityScale(ops.pure(1.0), EDataRef("kg")),
                    EIndicatorSpec("climate_change"),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_productWithoutAllocation_should_return_100percent_allocation() {
        // given
        val file = parseFile(
            "carrot", """
            ${UnitFixture.basicUnits}
            process carrot {
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.getTemplate("carrot") as EProcessTemplate).body).products[0]
        val unitsSymbolTable = SymbolTable(
            data = symbolTable.data
        )
        val expected = ETechnoExchange(
            EQuantityScale(ops.pure(1.0), EDataRef("kg")),
            EProductSpec(
                "carrot",
                EUnitOf(EQuantityClosure(unitsSymbolTable, EQuantityScale(ops.pure(1.0), EDataRef("kg"))))
            ),
            EQuantityScale(ops.pure(100.0), EDataRef("percent"))
        )
        assertEquals(expected, actual)
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
            sequenceOf(file),
            ops,
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.getTemplate("carrot") as EProcessTemplate).body).products[0]
        val unitsSymbolTable = SymbolTable(
            data = symbolTable.data
        )
        val expect = ETechnoExchange(
            EQuantityScale(ops.pure(1.0), EDataRef("kg")),
            EProductSpec(
                "carrot",
                EUnitOf(EQuantityClosure(unitsSymbolTable, EQuantityScale(ops.pure(1.0), EDataRef("kg"))))
            ),
            EQuantityScale(ops.pure(10.0), EDataRef("percent"))
        )
        assertEquals(expect, actual)
    }

    @Test
    fun testParse_whenDivideThenMultiply_shouldFactorCorrectly() {
        // given
        val file = parseFile(
            "maths", """
                    process p {
                        variables {
                            r = 20 u / 10 u * 2 u
                        }
                        products {
                            1 p product
                        }
                        emissions {
                            r result
                        }
                    }
                """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        val expected: QuantityExpression<BasicNumber> = EQuantityMul(
            EQuantityDiv(
                EQuantityScale(ops.pure(20.0), EDataRef("u")),
                EQuantityScale(ops.pure(10.0), EDataRef("u"))
            ),
            EQuantityScale(ops.pure(2.0), EDataRef("u"))
        )

        // when
        val symbolTable = parser.load()

        // then
        val template: EProcessTemplate<BasicNumber> = symbolTable.getTemplate("p")
            ?: throw Exception("template fetching barfed")
        val local = template.locals["r"] ?: throw Exception("locals barfed")
        assertEquals(expected, local)

    }

    @Test
    fun testParse_whenMultiplyThenDivide_shouldFactorCorrectly() {
        // given
        val file = parseFile(
            "maths", """
                    process p {
                        variables {
                            r = 10 u * 2 u / 20 u
                        }
                        products {
                            1 p product
                        }
                        emissions {
                            r result
                        }
                    }
                """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        val expected: QuantityExpression<BasicNumber> = EQuantityDiv(
            EQuantityMul(
                EQuantityScale(ops.pure(10.0), EDataRef("u")),
                EQuantityScale(ops.pure(2.0), EDataRef("u"))
            ),
            EQuantityScale(ops.pure(20.0), EDataRef("u"))
        )

        // when
        val symbolTable = parser.load()

        // then
        val template: EProcessTemplate<BasicNumber> = symbolTable.getTemplate("p")
            ?: throw Exception("template fetching barfed")
        val local = template.locals["r"] ?: throw Exception("locals barfed")
        assertEquals(expected, local)

    }

    @Test
    fun testParse_whenDimensionPow_thenScaleUntouched() {
        // given
        val file = parseFile(
            "maths", """
                package testParse_whenDimensionPow_thenScaleUntouched

                variables {
                    r = 10 m^2
                }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        val expected: QuantityExpression<BasicNumber> = EQuantityScale(
            ops.pure(10.0),
            EQuantityPow(
                EDataRef("m"),
                2.0
            )
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.data["r"]!!
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_whenQuantityExpressionPow_thenScaleIsTouched() {
        // given
        val file = parseFile(
            "maths", """
                package testParse_whenDimensionPow_thenScaleUntouched

                variables {
                    r = (10 m)^2
                }
            """.trimIndent()
        ) as LcaFile

        val expected: QuantityExpression<BasicNumber> = EQuantityPow(
            EQuantityScale(
                ops.pure(10.0),
                EDataRef("m")
            ),
            2.0
        )
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.data["r"]!!
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_processWithImpacts_shouldParse() {
        val pkgName = {}.javaClass.enclosingMethod.name
        val file = parseFile(
            "$pkgName.lca", """
                package $pkgName
                
                process a {
                    products {
                        1 kg x
                    }
                    impacts {
                        1 u cc
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file),
            ops,
        )
        val symbolTable = parser.load()

        // when
        val template = symbolTable.getTemplate("a") as ProcessTemplateExpression<BasicNumber>
        val actual =
            (ProcessTemplateExpression.eProcessTemplate<BasicNumber>().body().impacts() compose
                    Every.list() compose EImpact.indicator()).firstOrNull(template)

        // then
        assertEquals("cc", actual?.name)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
