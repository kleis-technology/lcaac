package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test

class LcaQuantityAnnotatorTest: BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testAnnotate_whenNotFound_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_whenNotFound_shouldAnnotate"
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            variables {
                x = q
            }
        """.trimIndent())
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaQuantityAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved quantity q") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFound_shouldDoNothing"
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            variables {
                q = 1 kg
                x = q
            }
        """.trimIndent())
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaQuantityAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFoundInPrelude_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFoundInPrelude_shouldDoNothing"
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            variables {
                x = 3 kg
            }
        """.trimIndent())
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getValue()
            .getTerm()
            .getFactor()
            .getPrimitive()
            .getRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaQuantityAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
