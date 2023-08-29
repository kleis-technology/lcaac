package ch.kleis.lcaplugin.e2e

import ch.kleis.lcaplugin.actions.csv.CsvProcessor
import ch.kleis.lcaplugin.actions.csv.CsvRequest
import ch.kleis.lcaplugin.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.expression.EQuantityScale
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class E2ETest : BasePlatformTestCase() {
    private val ops = BasicOperations

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_patternMatching() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production match (geo = "FR")
                    }
                }
                
                process water_production {
                    labels {
                        geo = "UK"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }

                process water_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        10 kg co2
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()

        // when
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.get("carrot from p{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.value(output, input)

        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals(10.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_patternMatching_withIndirection() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg intermediate from q(geo = "FR")
                    }
                }
                
                process q {
                    params {
                        geo = "UK"
                    }
                    products {
                        1 kg intermediate
                    }
                    inputs {
                        1 l water from water_production match (geo = geo)
                    }
                }
                
                process water_production {
                    labels {
                        geo = "UK"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }

                process water_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        10 kg co2
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()

        // when
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.get("carrot from p{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.value(output, input)

        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals(10.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_stringArgumentIndirect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    variables {
                        geo = "FR"
                    }
                    inputs {
                        1 l water from water_production(geo = geo)
                    }
                }

                process water_production {
                    params {
                        geo = "GLO"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()

        // when/then does not throw
        symbolTable.getTemplate("p")
            ?.let { Evaluator(symbolTable, ops).eval(EProcessTemplateApplication(template = it)) }!!
    }

    @Test
    fun test_stringArgument() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production(geo = "FR")
                    }
                }

                process water_production {
                    params {
                        geo = "GLO"
                    }
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()

        // when/then does not throw
        symbolTable.getTemplate("p")
            ?.let { Evaluator(symbolTable, ops).eval(EProcessTemplateApplication(template = it)) }!!
    }

    @Test
    fun test_csvProcessor() {
        // given
        val pkgName = "test_exponentiationPriority"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        a = 0 kg
                        b = 0 kg
                        c = 1 kg
                    }
                    products {
                        1 kg out
                    }
                    inputs {
                        a + b + c in
                    }
                }
            """.trimIndent()
        )
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val csvProcessor = CsvProcessor(symbolTable)
        val request = CsvRequest(
            "p",
            emptyMap(),
            mapOf("geo" to 0, "id" to 1, "a" to 2, "b" to 2),
            listOf("UK", "s00", "1.0", "1.0"),
        )

        // when
        val actual = csvProcessor.process(request)

        // then
        assertEquals(request, actual.request)
        val out = ProductValue(
            "out", kg,
            FromProcessRefValue(
                name = "p",
                arguments = mapOf(
                    "a" to QuantityValue(ops.pure(1.0), kg),
                    "b" to QuantityValue(ops.pure(1.0), kg),
                    "c" to QuantityValue(ops.pure(1.0), kg),
                )
            )
        )
        assertEquals(
            out, actual.output
        )
        val key = ProductValue(
            "in", kg,
        )
        assertEquals(
            QuantityValue(ops.pure(3.0), kg), actual.impacts[key]
        )
    }

    @Test
    fun test_exponentiationPriority() {
        // given
        val pkgName = "test_exponentiationPriority"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                variables {
                    x = 10 m^2
                }

                process p {
                    products {
                        1 kg foo
                    }
                    inputs {
                        2 x^2 bar
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val reducer = DataExpressionReducer(symbolTable.data, ops)
        val expr = symbolTable.getTemplate("p")!!.body.inputs.first().quantity

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EQuantityScale(ops.pure(200.0), EUnitLiteral(UnitSymbol.of("m").pow(4.0), 1.0, Prelude.length.pow(4.0)))
        TestCase.assertEquals(expected, actual)
    }


    @Test
    fun test_substanceResolution() {
        val pkgName = "e2e.test_substanceResolution"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg b(compartment="compartment")
                    }
                }
                
                substance b {
                    name = "b"
                    type = Emission
                    compartment = "compartment"
                    reference_unit = kg
                    
                    impacts {
                        1 kg co2
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.getElements().first()
        val cf = result.value(output, input)

        assertEquals("a from p{}{}", output.getDisplayName())
        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals("co2", input.getDisplayName())
        assertEquals(1.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_meta_whenKeywordAsKey() {
        // given
        val pkgName = "e2e.test_meta_whenKeywordAsKey"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    meta {
                        "unit" = "a"
                        "process" = "b"
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile

        // when
        val actual = file.getProcesses().first().getBlockMetaList().first().metaAssignmentList

        // then
        assertEquals("unit", actual[0].getName())
        assertEquals("a", actual[0].getValue())
        assertEquals("process", actual[1].getName())
        assertEquals("b", actual[1].getValue())
    }

    @Test
    fun test_operationPriority() {
        // given
        val pkgName = "e2e.test_operationPriority"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
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
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.getElements().first()
        val cf = result.value(output, input)

        assertEquals("out from p{}{}", output.getDisplayName())
        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals("in", input.getDisplayName())
        assertEquals(6.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.length.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_twoInstancesSameTemplate_whenOneImplicit() {
        // given
        val pkgName = "e2e.test_twoInstancesSameTemplate_whenOneImplicit"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
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
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("office")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.value(output, input)

        assertEquals("office from office{}{}", output.getDisplayName())
        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(Dimension.None.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals("co2", input.getDisplayName())
        assertEquals(3.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)

        fun asValue(unit: EUnitLiteral<BasicNumber>): UnitValue<BasicNumber> {
            return UnitValue(unit.symbol, unit.scale, unit.dimension)
        }

        val ratio = result.valueRatio(output, input)
        assertEquals(QuantityValue(ops.pure(3.0), asValue(UnitFixture.kg)), ratio)
    }

    @Test
    fun test_twoInstancesSameTemplate_whenExplicit() {
        // given
        val pkgName = "e2e.test_twoInstancesSameTemplate_whenExplicit"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
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
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("office")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.value(output, input)

        assertEquals("office from office{}{}", output.getDisplayName())
        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(Dimension.None.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals("co2", input.getDisplayName())
        assertEquals(3.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_manyInstancesSameTemplate() {
        // given
        val pkgName = "e2e.test_manyInstancesSameTemplate"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
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
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("office")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.value(output, input)

        assertEquals("office from office{}{}", output.getDisplayName())
        assertEquals(1.0, cf.output.quantity().amount.value)
        assertEquals(Dimension.None.getDefaultUnitValue<BasicNumber>(), cf.output.quantity().unit)

        assertEquals("co2", input.getDisplayName())
        assertEquals(13.0, cf.input.quantity().amount.value)
        assertEquals(DimensionFixture.mass.getDefaultUnitValue<BasicNumber>(), cf.input.quantity().unit)
    }

    @Test
    fun test_allocate() {
        // given
        val pkgName = "e2e.test_allocate"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out1 allocate 90 percent
                    1 kg out2 allocate 10 percent
                }
                inputs {
                    1 kg in
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().impactFactors
        val output1 = result.observablePorts.getElements()[0]
        val output2 = result.observablePorts.getElements()[1]
        val input = result.controllablePorts.getElements().first()
        val cf1 = result.value(output1, input)
        val cf2 = result.value(output2, input)

        val delta = 1E-9
        assertEquals(0.9, cf1.input.quantity().amount.value, delta)
        assertEquals(0.1, cf2.input.quantity().amount.value, delta)
    }

    @Test
    fun test_allocate_whenOneProduct_allocateIsOptional() {
        // given
        val pkgName = "e2e.test_allocate_whenOneProduct_allocateIsOptional"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        // when
        val symbolTable = parser.load()
        val actual =
            (((symbolTable.getTemplate("p") as EProcessTemplate).body).products[0].allocation as EQuantityScale).scale
        // then
        assertEquals(100.0, actual.value)
    }

    @Test
    fun test_allocate_whenSecondaryBlock_EmptyBlockIsAllowed() {
        // given
        val pkgName = "e2e.test_allocate_whenSecondaryBlock_EmptyBlockIsAllowed"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
                products {
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        // when
        val symbolTable = parser.load()
        val actual =
            (((symbolTable.getTemplate("p") as EProcessTemplate).body).products[0].allocation as EQuantityScale).scale
        // then
        assertEquals(100.0, actual.value)
    }

    @Test
    fun test_allocate_whenTwoProducts_shouldReturnWeightedResult() {
        // given
        val pkgName = "e2e.test_allocate_whenTwoProducts_shouldReturnWeightedResult"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out allocate 20 percent
                    1 kg otherOut allocate 80 percent
                }
                inputs {
                    1 m3 water
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())

        // then
        val result = assessment.run().impactFactors
        val output1 = result.observablePorts.getElements()[0]
        val output2 = result.observablePorts.getElements()[1]
        val input = result.controllablePorts.getElements().first()
        val cf1 = result.value(output1, input)
        val cf2 = result.value(output2, input)

        val delta = 1E-9
        val expected1 = 1.0 * 20 / 100
        val expected2 = 1.0 * 80 / 100
        assertEquals(expected1, cf1.input.quantity().amount.value, delta)
        assertEquals(expected2, cf2.input.quantity().amount.value, delta)
    }

    @Test
    fun test_unitAlias_whenInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenInfiniteLoop_shouldThrowAnError"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            unit foo {
                symbol = "foo"
                alias_for = 1 foo
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val evaluator = Evaluator(symbolTable, ops)

        // when + then
        val e = assertFailsWith(EvaluatorException::class, null) { evaluator.eval(entryPoint) }
        assertEquals("Recursive dependency for unit foo", e.message)
    }

    @Test
    fun test_unitAlias_whenNestedInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenNestedInfiniteLoop_shouldThrowAnError"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 foo
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val evaluator = Evaluator(symbolTable, ops)

        // when + then
        val e = assertFailsWith(EvaluatorException::class, null) { evaluator.eval(entryPoint) }
        assertEquals("Recursive dependency for unit foo", e.message)
    }

    @Test
    fun test_unitAlias_shouldNotThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_shouldNotThrowAnError"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 kg
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val evaluator = Evaluator(symbolTable, ops)

        // when, then does not throw
        evaluator.eval(entryPoint)
    }

    @Test
    fun test_unitAlias_whenAdditionInAliasForField_shouldNotThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenAdditionInAliasForField_shouldNotThrowAnError"
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 kg
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar + 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)

        // when/then
        Evaluator(symbolTable, ops).eval(entryPoint)
    }

    @Test
    fun test_processImpact_whenImpactBlockInProcess_shouldEvaluate() {
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
                impacts {
                    1 u climate_change
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)

        // when
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p")!!)
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())

        // then
        val result = assessment.run().impactFactors
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.get("climate_change")
        val cf = result.value(output, input)

        val delta = 1E-9
        val expected = 1.0

        assertEquals("climate_change", input.getDisplayName())
        assertEquals(expected, cf.input.quantity().amount.value, delta)
        assertEquals(UnitValueFixture.unit, cf.input.quantity().unit)
    }

    @Test
    fun test_processInput_whenWrongUnit_thenShouldThrow() {
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p1 {
                products {
                    1 kg out
                }
                inputs {
                    1 l in
                }
            }
            process p2 {
                products {
                    1 kg in
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(template = symbolTable.getTemplate("p1")!!)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) {
            Evaluator(symbolTable, ops).trace(entryPoint)
        }
        assertEquals("incompatible dimensions: length³ vs mass for product in", e.message)
    }
}
