package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test

class LcaUnitAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_whenNotFound_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = unknown
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(project, "$pkgName.s").first()
            .getReferenceUnitField().getValue()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaUnitAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved unit unknown") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFoundInPrelude_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFoundInPrelude_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = kg
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(project, "$pkgName.s").first()
            .getReferenceUnitField().getValue()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaUnitAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFoundInExplicitDefinition_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFoundInExplicitDefinition_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = foo
            }
            
            unit foo {
                symbol = "foo"
                dimension = "foo"
            }
        """.trimIndent()
        )
        val element = SubstanceKeyIndex.findSubstances(project, "$pkgName.s").first()
            .getReferenceUnitField().getValue()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaUnitAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
