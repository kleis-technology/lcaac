package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class UnitReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/units"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = "language.psi.reference.units.test_resolve"
        val ref = SubstanceKeyIndex.findSubstances(project, "$pkgName.s").first()
            .getReferenceUnitField()
            .getValue()
            .getFactor()
            .getPrimitive()
            .getRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = UnitKeyIndex.findUnits(project, "$pkgName.my_units.foo").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = "language.psi.reference.units.test_resolve"
        val ref = SubstanceKeyIndex.findSubstances(project, "$pkgName.s").first()
            .getReferenceUnitField()
            .getValue()
            .getFactor()
            .getPrimitive()
            .getRef()

        // when
        val actual = ref.reference.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf("foo", "bar").sorted()
        TestCase.assertEquals(expected, actual)
    }
}
