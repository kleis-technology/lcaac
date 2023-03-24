package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
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
                any<StubIndexKey<String, PsiUnitDefinition>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiUnitDefinition>>(),
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
                any<StubIndexKey<String, PsiUnitDefinition>>(),
                any<String>(),
                any<Project>(),
                any<GlobalSearchScope>(),
                any<Class<PsiUnitDefinition>>(),
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
            
            substance s {
                name = "s"
                compartment = "c"
                reference_unit = foo
            }
        """.trimIndent()
        ) as LcaFile
        return file.getSubstances().first()
            .getReferenceUnitField()
            .getValue()
            .getFactor()
            .getPrimitive()
            .getRef()
    }

    private fun unitFoo(): PsiUnitDefinition {
        val file = parseFile(
            "xyz", """
            package xyz
            
            unit foo {
                symbol = "foo"
                dimension = "foo"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitDefinitions().first()
    }

    private fun unitBar(): PsiUnitDefinition {
        val file = parseFile(
            "xyz", """
            package xyz
            
            unit bar {
                symbol = "bar"
                dimension = "bar"
            }
        """.trimIndent()
        ) as LcaFile
        return file.getUnitDefinitions().first()
    }

    override fun getTestDataPath(): String {
        return ""
    }

}
