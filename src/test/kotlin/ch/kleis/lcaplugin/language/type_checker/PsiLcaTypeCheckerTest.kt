package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.lang.type.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class PsiLcaTypeCheckerTest : BasePlatformTestCase() {
    override
    fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_whenBioExchangeCompatibleRefUnitAndQuantityExpressionUnit_shouldTypeCheck() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca",
            """
            package $pkgName
            
            substance testSubstance {
                name = "testSubstance"
                type = Emission
                compartment = "air"
                reference_unit = kg

                impacts {
                    1 kg cc
                }
            }

            process testProcess {
                products {
                    1 kg testProduct
                }
                emissions {
                    1 kg testSubstance(compartment="air")
                }
            }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.testProcess").first()
            .getEmissions().first()
        val checker = PsiLcaTypeChecker()
        val expected = TBioExchange(TSubstance("testSubstance", DimensionFixture.mass, "air", null))

        // when
        val actual = checker.check(target)

        // then
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenBioExchangeNotCompatibleRefUnitAndQuantityExpressionUnit_shouldThrow() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca",
            """
            package $pkgName
            
            substance testSubstance {
                name = "testSubstance"
                type = Emission
                compartment = "air"
                reference_unit = kg

                impacts {
                    1 kg cc
                }
            }

            process testProcess {
                products {
                    1 kg testProduct
                }
                emissions {
                    1 l testSubstance(compartment="air")
                }
            }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.testProcess").first()
            .getEmissions().first()
        val checker = PsiLcaTypeChecker()
        val expected =
            "Incompatible dimensions: substance reference dimension is mass but exchange dimension is length³"

        // when + then
        val error = assertFailsWith(PsiTypeCheckException::class) { checker.check(target) }
        assertEquals(expected, error.message)
    }

    @Test
    fun test_whenMutuallyRecursiveQuantityExpression_shouldThrowTypeCheckException() {
        // given
        val pkgName = "test_whenMutuallyRecursiveQuantityExpression_shouldThrowTypeCheckException"
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                variables {
                    p = 95 percent
                    r = p * s
                    s = p * r
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.r")
            .first()
            .getValue()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(
            PsiTypeCheckException::class,
        ) { checker.check(target) }
    }

    @Test
    fun test_whenRecursiveQuantityExpression_shouldThrowTypeCheckException() {
        // given
        val pkgName = "test_whenRecursiveQuantityExpression_shouldThrowTypeCheckException"
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                variables {
                    p = 95 percent
                    r = p * r
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.r")
            .first()
            .getValue()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(
            PsiTypeCheckException::class,
        ) { checker.check(target) }
    }

    @Test
    fun test_whenCircularDependencyInUnitDefinition_shouldThrowTypeCheckException() {
        // given
        val pkgName = """test_whenCircularDependencyInUnitDefinition_shouldThrowTypeCheckException"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    alias_for = 1 bar
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 1 foo
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.foo").first()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(
            PsiTypeCheckException::class,
            """circular dependencies: "1 bar ...", "1 bar ...", "1 bar ...", "1 foo ...", "1 foo ...", "1 foo ...", "b""",
        ) { checker.check(target) }
    }

    @Test
    fun test_whenUnitLiteral() {
        // given
        val pkgName = """test_whenUnitLiteral"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.foo").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias() {
        // given
        val pkgName = """test_whenUnitAlias"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_div() {
        // given
        val pkgName = """test_whenUnitAlias_div"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo / 3 foo2
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim").divide(Dimension.of("foo2_dim")))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_mul() {
        // given
        val pkgName = """test_whenUnitAlias_mul"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo * 3 foo2
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim").multiply(Dimension.of("foo2_dim")))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_addition_sameDim() {
        // given
        val pkgName = """test_whenUnitAlias_addition_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo + 3 foo
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_subtraction_sameDim() {
        // given
        val pkgName = """test_whenUnitAlias_subtraction_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo - 3 foo
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_addition_differentDim_shouldThrow() {
        // given
        val pkgName = """test_whenUnitAlias_addition_differentDim_shouldThrow"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 5 foo + 3 foo2
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(PsiTypeCheckException::class, "incompatible dimensions: foo_dim vs foo2_dim") {
            checker.check(target)
        }
    }

    @Test
    fun test_whenUnitAlias_withParenthesis() {
        // given
        val pkgName = """test_whenUnitAlias_withParenthesis"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = (5 foo)^2
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim").pow(2.0))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_refToGlobalAssignment() {
        // given
        val pkgName = """test_whenUnitAlias_refToGlobalAssignment"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                variables {
                    x = 2 foo
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = 3 x
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenUnitAlias_directRef() {
        // given
        val pkgName = """test_whenUnitAlias_directRef"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                variables {
                    x = 2 foo
                }
                
                unit bar {
                    symbol = "bar"
                    alias_for = x
                }
            """.trimIndent()
        )
        val target = UnitStubKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TUnit(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment() {
        // given
        val pkgName = """test_whenUnitAlias_directRef"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                variables {
                    x = 2 foo
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment_addition_sameDim() {
        // given
        val pkgName = """test_whenGlobalAssignment_addition_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                variables {
                    x = 2 foo + 4 foo
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment_sub_sameDim() {
        // given
        val pkgName = """test_whenGlobalAssignment_addition_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                variables {
                    x = 2 foo - 4 foo
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment_mul() {
        // given
        val pkgName = """test_whenGlobalAssignment_addition_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                variables {
                    x = 2 foo * 4 foo2
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim").multiply(Dimension.of("foo2_dim")))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment_div() {
        // given
        val pkgName = """test_whenGlobalAssignment_addition_sameDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                variables {
                    x = 2 foo / 4 foo2
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim").divide(Dimension.of("foo2_dim")))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenGlobalAssignment_addition_differentDim() {
        // given
        val pkgName = """test_whenGlobalAssignment_addition_differentDim"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit foo2 {
                    symbol = "foo2"
                    dimension = "foo2_dim"
                }
                
                variables {
                    x = 2 foo + 4 foo2
                }
            """.trimIndent()
        )
        val target = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(PsiTypeCheckException::class, "incompatible dimensions: foo_dim vs foo2_dim") {
            checker.check(
                target
            )
        }
    }

    @Test
    fun test_whenLocalAssignment() {
        // given
        val pkgName = """test_whenLocalAssignment"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                process p {
                    variables {
                        x = 2 foo
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getLcaVariables().first()
            .assignmentList.first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenParameter() {
        // given
        val pkgName = """test_whenLocalAssignment"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                process p {
                    params {
                        x = 2 foo
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getLcaParams().first()
            .assignmentList.first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TQuantity(Dimension.of("foo_dim"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenTechnoInputExchange_unresolvedProduct_shouldReturnDimensionOfGivenQuantity() {
        // given
        val pkgName = """test_whenLocalAssignment"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                process p {
                    inputs {
                        1 foo foo_product
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TTechnoExchange(TProduct("foo_product", Dimension.of("foo_dim")))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenTechnoInputExchange_resolvedProduct_incompatibleDims_shouldThrow() {
        // given
        val pkgName = """test_whenLocalAssignment"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit kg {
                    symbol = "kg"
                    dimension = "mass"
                }
                
                process p {
                    inputs {
                        1 foo foo_product
                    }
                }
                
                process foo_prod {
                    products {
                        1 kg foo_product
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(
            PsiTypeCheckException::class,
            "incompatible dimensions: foo_dim vs mass"
        ) { checker.check(target) }
    }

    @Test
    fun test_whenTechnoInputExchange_invalidLabelValue_shouldThrow() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit kg {
                    symbol = "kg"
                    dimension = "mass"
                }
                
                process p {
                    inputs {
                        1 kg foo_product from foo_prod match (geo = 1 kg)
                    }
                }
                
                process foo_prod {
                    params {
                        x = 1 foo
                    }
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 kg foo_product
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val checker = PsiLcaTypeChecker()

        // when/then
        val e = assertFailsWith(
            PsiTypeCheckException::class
        ) { checker.check(target) }
        assertEquals("incompatible types: expecting TString, found TQuantity(dimension=mass)", e.message)
    }

    @Test
    fun test_whenTechnoInputExchange_wrongDimInArgument_shouldThrow() {
        // given
        val pkgName = """test_whenTechnoInputExchange_wrongDimInArgument_shouldThrow"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    dimension = "foo_dim"
                }
                
                unit kg {
                    symbol = "kg"
                    dimension = "mass"
                }
                
                process p {
                    inputs {
                        1 kg foo_product from foo_prod(x = 2 kg)
                    }
                }
                
                process foo_prod {
                    params {
                        x = 1 foo
                    }
                    products {
                        1 kg foo_product
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val checker = PsiLcaTypeChecker()

        // when/then
        assertFailsWith(
            PsiTypeCheckException::class,
            "incompatible dimensions: expecting foo_dim, found mass"
        ) { checker.check(target) }
    }

    @Test
    fun test_whenPreludeUnit() {
        // given
        val pkgName = """test_whenPreludeUnit"""
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    inputs {
                        1 kg foo_product
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val checker = PsiLcaTypeChecker()

        // when
        val actual = checker.check(target)

        // then
        val expected = TTechnoExchange(
            TProduct("foo_product", Prelude.mass)
        )
        TestCase.assertEquals(expected, actual)
    }
}
