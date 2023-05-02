package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class SubstanceReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/subst"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = "language.psi.reference.subst.test_resolve"
        val substanceSpec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference.resolve()

        // then
        val expected = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.co2_air.co2_air",
            "Emission",
            "air"
        ).first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = "language.psi.reference.subst.test_resolve"
        val spec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .getSubstanceSpec()

        // when
        val actual = spec.reference
            .variants.map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf(
            """co2_air(type="Emission", compartment="air")""",
            """another_co2_air(type="Emission", compartment="air", sub_compartment="another")""",
        ).sorted()
        TestCase.assertEquals(expected, actual)
    }
}
