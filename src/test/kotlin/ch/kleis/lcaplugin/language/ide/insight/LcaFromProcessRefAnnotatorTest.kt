package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.*
import org.junit.Test

class LcaFromProcessRefAnnotatorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val element = fromCarrotProductionRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaFromProcessRefAnnotator()

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiProcess>>(),
                any(),
                any(),
                any(),
                any<Class<PsiProcess>>(),
            )
        } returns emptyList()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved process carrot_production") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        // given
        val element = fromCarrotProductionRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaFromProcessRefAnnotator()

        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiProcess>>(),
                any(),
                any(),
                any(),
                any<Class<PsiProcess>>(),
            )
        } answers {
            val target = it.invocation.args[1] as String
            if (target == "carrot.carrot_production") {
                listOf(carrotProduction())
            } else emptyList()
        }

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun fromCarrotProductionRef(): PsiProcessTemplateRef {
        val file = parseFile(
            "abc", """
            package abc
            
            import carrot
            
            process w {
                inputs {
                    1 kg carrot from carrot_production()
                }
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first().getInputs().first()
            .getFromProcessConstraint()!!.getProcessTemplateRef()
    }

    private fun carrotProduction(): PsiProcess {
        val file = parseFile(
            "carrot", """
            package carrot
            
            process carrot_production {
            }
        """.trimIndent()
        ) as LcaFile
        return file.getProcesses().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
