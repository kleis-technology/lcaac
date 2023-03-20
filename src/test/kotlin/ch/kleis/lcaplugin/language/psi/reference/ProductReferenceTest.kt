package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
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


class ProductReferenceTest: ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_resolve() {
        // given
        val file = parseFile(
            "hello",
            """
               import abc
               
               process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg water
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getInputs().first().getProductRef().reference as PsiReference
        val abcWater = abcWater()
        val exchanges = listOf(abcWater, xyzWater())

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiTechnoProductExchange>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiTechnoProductExchange>>(),
            )
        } returns exchanges

        // when
        val actual = ref.resolve()

        // then
        TestCase.assertEquals(abcWater, actual)

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun abcWater(): PsiTechnoProductExchange {
        val file = parseFile(
            "abc", """
            package abc
            
            process w {
                products {
                    1 kg water
                }
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first().getProducts().first()
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
                    inputs {
                        1 kg car
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getInputs().first().getProductRef().reference as PsiReference

        val stubIndex = mockk<StubIndex>()
        mockkStatic(StubIndex::class)
        every {StubIndex.getInstance()} returns stubIndex
        val results = listOf("carrot", "auto_car", "salad", "computer")
        every {
            stubIndex.getAllKeys(
                any<StubIndexKey<String, PsiTechnoProductExchange>>(),
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

    private fun xyzWater(): PsiTechnoProductExchange {
        val file = parseFile(
            "xyz", """
            package xyz
            
            process w {
                products {
                    1 kg water
                }
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first().getProducts().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
