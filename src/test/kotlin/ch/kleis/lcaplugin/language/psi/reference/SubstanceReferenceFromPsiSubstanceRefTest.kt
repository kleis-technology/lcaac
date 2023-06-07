package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class SubstanceReferenceFromPsiSubstanceRefTest : BasePlatformTestCase() {
    @Test
    fun test_resolve_whenInsideSubstanceDefinition() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                substance s {
                    name = "s"
                    type = Emission
                    compartment = "c"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        val substance = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s",
            SubstanceType.EMISSION.value,
            "c"
        ).first()
        val ref = substance.getSubstanceRef()

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(substance, actual)
    }

    @Test
    fun test_resolve_whenInsideSubstanceSpec() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    emissions {
                        1 kg s(compartment="c")
                    }
                }
                
                substance s {
                    name = "s"
                    type = Emission
                    compartment = "c"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .substanceSpec
            .getSubstanceRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.s",
            SubstanceType.EMISSION.value,
            "c"
        ).first()
        assertEquals(expected, actual)
    }
}
