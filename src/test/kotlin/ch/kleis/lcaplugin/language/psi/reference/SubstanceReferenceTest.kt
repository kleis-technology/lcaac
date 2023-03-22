package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Test

class SubstanceReferenceTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_resolve() {
        // given
        val file = parseFile(
            "hello",
            """
               import ef31
               
               process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2_air
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getEmissions().first().getSubstanceRef().reference as PsiReference
        val ef31Co2Air = ef31Co2Air()
        val substances = listOf(ef31Co2Air, ef32Co2Air())

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiSubstance>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiSubstance>>(),
            )
        } returns substances

        // when
        val actual = ref.resolve()

        // then
        TestCase.assertEquals(ef31Co2Air, actual)

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun test_getVariants() {
        // given
        val file = parseFile(
            "hello",
            """
               import ef31
               
               process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getEmissions().first().getSubstanceRef().reference as PsiReference

        val stubIndex = mockk<StubIndex>()
        mockkStatic(StubIndex::class)
        every {StubIndex.getInstance()} returns stubIndex
        val results = listOf("co2_air", "water_co2", "propanol_air", "water_propanol")
        every {
            stubIndex.getAllKeys(
                any<StubIndexKey<String, PsiSubstance>>(),
                any<Project>(),
            )
        } returns results

        // when
        val actual = ref.variants.toList().map { (it as LookupElementBuilder).lookupString }

        // then
        TestCase.assertEquals(results, actual)

        // clean
        unmockkStatic(StubIndex::class)
    }

    private fun ef31Co2Air(): PsiSubstance {
        val file = parseFile(
            "substances", """
            package ef31
            
            substance co2_air {
                name = "co2"
                compartment = "air"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        return file.getSubstances().first()
    }

    private fun ef32Co2Air(): PsiSubstance {
        val file = parseFile(
            "substances", """
            package ef32
            
            substance co2_air {
                name = "co2"
                compartment = "air"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        return file.getSubstances().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}