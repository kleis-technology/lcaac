package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.*
import org.junit.Test

class LcaTechnoInputExchangeAnnotatorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val element = technoInputExchange()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


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
        } returns emptyList()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved product electricity") }
        verify { mock.builder.range(element.getProductRef()) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        // given
        val element = technoInputExchange()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


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
        } returns listOf(electricity())

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun technoInputExchange(): PsiTechnoInputExchange {
        val file = parseFile(
            "abc", """
            package abc
            
            import xyz
            
            process w {
                products {
                    1 kg water
                }
                inputs {
                    1 kWh electricity
                }
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first().getInputs().first()
    }

    private fun electricity(): PsiTechnoProductExchange {
        val file = parseFile(
            "electricity", """
                package xyz
                
                process p {
                    products {
                        1 kWh electricity
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
