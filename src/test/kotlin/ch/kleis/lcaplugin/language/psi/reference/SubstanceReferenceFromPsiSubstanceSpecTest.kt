package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test

class SubstanceReferenceFromPsiSubstanceSpecTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_resolve_whenNoSubCompartment_shouldDefaultToMatchingCompartment() {
        // given
        val pkgName =
            "language.psi.reference.subst.test_resolve_whenNoSubCompartment_shouldDefaultToMatchingCompartment"
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2(compartment="air", sub_compartment="nothing")
                    }
                }
            """.trimIndent()
        )
        val substanceSpec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference.resolve()

        // then
        val expected = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.co2_air.co2",
            "Emission",
            "air"
        ).first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenIncompatibleTypes_ShouldNotResolve() {
        // given
        val pkgName = "language.psi.reference.subst.test_resolve_whenIncompatibleTypes_ShouldNotResolve"
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    resources {
                        1 kg co2(compartment="air", sub_compartment="nothing")
                    }
                }
            """.trimIndent()
        )
        val substanceSpec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getResources().first()
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference.resolve()

        // then
        assertNull(actual)
    }

    @Test
    fun test_resolve__whenExact() {
        // given
        val pkgName = "language.psi.reference.subst.test_resolve__whenExact"
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2(compartment="air")
                    }
                }
            """.trimIndent()
        )
        val substanceSpec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference.resolve()

        // then
        val expected = SubstanceKeyIndex.findSubstances(
            project,
            "$pkgName.co2_air.co2",
            "Emission",
            "air"
        ).first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = "language.psi.reference.subst.test_getVariants"
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2_air {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance another_co2_air {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2_air(compartment="air")
                    }
                }
            """.trimIndent()
        )
        val spec = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
            .getSubstanceSpec()

        // when
        val actual = spec.reference
            .variants.map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf(
            """co2_air(compartment="air")""",
            """another_co2_air(compartment="air", sub_compartment="another")""",
        ).sorted()
        TestCase.assertEquals(expected, actual)
    }
}
