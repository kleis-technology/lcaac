package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaFromProcessRefAnnotatorTest : BasePlatformTestCase() {

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
            
            process p {
                inputs {
                    1 kg carrot from carrot_prod()
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .fromProcessConstraint!!
            .processTemplateRef!!
        val mock = AnnotationHolderMock()
        val annotator = LcaFromProcessRefAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "unresolved process carrot_prod") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        // given
        val pkgName = "testAnnotate_whenFound_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    1 kg carrot from carrot_prod()
                }
            }
            
            process carrot_prod {
                products {
                   1 kg carrot
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .fromProcessConstraint!!
            .processTemplateRef!!
        val mock = AnnotationHolderMock()
        val annotator = LcaFromProcessRefAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
