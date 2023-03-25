package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
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

class ProcessReferenceTest: ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_resolve() {
        val file = parseFile(
            "hello",
            """
               package hello
               
               import carrot
               
               process p {
                    products {
                        1 kg a
                    }
                    inputs {
                        1 kg x from carrot_production()
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getInputs().first()
            .getFromProcessConstraint()?.getProcessTemplateRef()?.reference as PsiReference
        val carrotProduction = carrotProduction()
        val processes = listOf(carrotProduction, saladProduction())

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiProcess>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiProcess>>(),
            )
        } answers {
            val target = it.invocation.args[1]
            if (target == "carrot.carrot_production") {
                listOf(carrotProduction)
            } else emptyList()
        }

        // when
        val actual = ref.resolve()

        // then
        TestCase.assertEquals(carrotProduction, actual)

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
               package hello
               
               import carrot
               
               process p {
                    products {
                        1 kg a
                    }
                    inputs {
                        1 kg x from ca()
                    }
               }
            """.trimIndent()
        ) as LcaFile
        val ref = file.getProcesses().first().getInputs().first()
            .getFromProcessConstraint()?.getProcessTemplateRef()?.reference as PsiReference

        val stubIndex = mockk<StubIndex>()
        mockkStatic(StubIndex::class)
        every { StubIndex.getInstance() } returns stubIndex
        val results = listOf("carrot.carrot_production", "carrot.salad_production", "carrot.another_process")
        every {
            stubIndex.getAllKeys(
                any<StubIndexKey<String, PsiProcess>>(),
                any<Project>()
            )
        } returns results

        // when
        val actual = ref.variants.toList().map { (it as LookupElementBuilder).lookupString }

        // then
        val expected = results.map { it.split(".").last() }
        TestCase.assertEquals(expected, actual)

        // clean
        unmockkStatic(StubIndex::class)
    }

    private fun carrotProduction(): PsiProcess {
        val file = parseFile(
            "processes", """
                package carrot
                
                process carrot_production {
                }
            """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first()
    }

    private fun saladProduction(): PsiProcess {
        val file = parseFile(
            "processes", """
                package salad
                
                process salad_production {
                }
            """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
