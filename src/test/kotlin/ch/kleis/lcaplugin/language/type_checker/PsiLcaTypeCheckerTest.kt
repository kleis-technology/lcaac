package ch.kleis.lcaplugin.language.type_checker

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.type.TProduct
import ch.kleis.lcaplugin.core.lang.type.TQuantity
import ch.kleis.lcaplugin.core.lang.type.TTechnoExchange
import ch.kleis.lcaplugin.core.lang.type.TUnit
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class PsiLcaTypeCheckerTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.foo").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
        val checker = PsiLcaTypeChecker()

        // when/then
        try {
            checker.check(target)
            fail("should have thrown")
        } catch (e: PsiTypeCheckException) {
            TestCase.assertEquals("incompatible dimensions: foo_dim[1.0] vs foo2_dim[1.0]", e.message)
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        val target = UnitKeyIndex.findUnits(project, "$pkgName.bar").first()
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
        try {
            checker.check(target)
            fail("should have thrown")
        } catch (e: PsiTypeCheckException) {
            TestCase.assertEquals("incompatible dimensions: foo_dim[1.0] vs foo2_dim[1.0]", e.message)
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
            .getPsiVariablesBlocks().first()
            .getAssignments().first()
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
            .getPsiParametersBlocks().first()
            .getAssignments().first()
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
        try {
            checker.check(target)
            fail("should have thrown")
        } catch (e: PsiTypeCheckException) {
            TestCase.assertEquals("incompatible dimensions: foo_dim[1.0] vs mass[1.0]", e.message)
        }
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
        try {
            checker.check(target)
            fail("should have thrown")
        } catch (e: PsiTypeCheckException) {
            TestCase.assertEquals("incompatible dimensions: expecting foo_dim[1.0], found mass[1.0]", e.message)
        }
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
