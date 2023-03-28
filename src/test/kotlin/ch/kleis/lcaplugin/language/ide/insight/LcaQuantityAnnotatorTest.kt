package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.*
import org.junit.Test

class LcaQuantityAnnotatorTest: ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val element = quantityFooRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaQuantityAnnotator()


        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiGlobalAssignment>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiGlobalAssignment>>(),
            )
        } returns emptyList()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved quantity foo") }
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
        val element = quantityFooRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiGlobalAssignment>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiGlobalAssignment>>(),
            )
        } returns listOf(quantityFoo(), quantityBar())

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun quantityFooRef(): PsiQuantityRef {
        val file = parseFile(
            "abc", """
            package abc
            
            import xyz
            
            variables {
                x = foo
            }
        """.trimIndent()
        ) as LcaFile
        return file.getGlobalAssignments().first()
            .second
            .getTerm().getFactor().getPrimitive().getRef()
    }

    private fun quantityFoo(): PsiGlobalAssignment {
        val file = parseFile(
            "xyz", """
            package xyz
            
            variables {
                foo = 1 kg
            }
        """.trimIndent()
        ) as LcaFile
        return file.getPsiGlobalVariablesBlocks()
            .first()
            .getGlobalAssignments().first()
    }

    private fun quantityBar(): PsiGlobalAssignment {
        val file = parseFile(
            "xyz", """
            package xyz
            
            variables {
                bar = 1 kg
            }
        """.trimIndent()
        ) as LcaFile
        return file.getPsiGlobalVariablesBlocks()
            .first()
            .getGlobalAssignments().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }

}
