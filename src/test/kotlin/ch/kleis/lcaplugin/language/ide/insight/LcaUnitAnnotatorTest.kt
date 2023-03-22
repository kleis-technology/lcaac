package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.testFramework.ParsingTestCase
import io.mockk.*
import org.junit.Test

class LcaUnitAnnotatorTest: ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val element = unitFooRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaUnitAnnotator()


        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiUnitLiteral>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiUnitLiteral>>(),
            )
        } returns emptyList()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved unit foo") }
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
        val element = unitFooRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        mockkStatic(GlobalSearchScope::class)
        every { GlobalSearchScope.allScope(any()) } returns mockk()

        mockkStatic(StubIndex::class)
        every {
            StubIndex.getElements(
                any<StubIndexKey<String, PsiUnitLiteral>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiUnitLiteral>>(),
            )
        } returns listOf(unitFoo(), unitBar())

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }

        // clean
        unmockkStatic(GlobalSearchScope::class)
        unmockkStatic(StubIndex::class)
    }

    private fun unitFooRef(): PsiUnitRef {
        val file = parseFile(
            "abc", """
            package abc
            
            import xyz
            
            variables {
                x = 1 foo
            }
        """.trimIndent()
        ) as LcaFile
        return file.getPsiVariablesBlocks().first()
            .getEntries()
            .first()
            .second
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getUnit()
            .getFactor()
            .getPrimitive()
            .getRef()
    }

    private fun unitFoo(): PsiUnitLiteral {
        val file = parseFile(
            "xyz", """
            package xyz
            
            unit foo {
                symbol = "foo"
                scale = 1.0
                dimension = "foo"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitLiterals().first()
    }

    private fun unitBar(): PsiUnitLiteral {
        val file = parseFile(
            "xyz", """
            package xyz
            
            unit bar {
                symbol = "bar"
                scale = 1.0
                dimension = "bar"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitLiterals().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }

}
