package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.*
import org.junit.Test

class LcaBioExchangeAnnotatorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val element = bioExchange()
        val mock = AnnotationHolderMock()
        val annotator = LcaBioExchangeAnnotator()


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
        } returns emptyList()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved substance co2") }
        verify { mock.builder.range(element.getSubstanceRef()) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        // given
        val element = bioExchange()
        val mock = AnnotationHolderMock()
        val annotator = LcaBioExchangeAnnotator()


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
        } returns listOf(co2())

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun bioExchange(): PsiBioExchange {
        val file = parseFile(
            "abc", """
            package abc
            
            process w {
                products {
                    1 kg water
                }
                emissions {
                    1 kg co2
                }
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first().getEmissions().first()
    }

    private fun co2(): PsiSubstance {
        val file = parseFile(
            "abc", """
            package abc
            
            substance co2 {
                name = "co2"
                compartment = "compartment"
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
