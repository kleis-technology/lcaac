package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("DialogTitleCapitalization")
@RunWith(JUnit4::class)
class LcaTechnoInputExchangeAnnotatorTest : BasePlatformTestCase() {

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
                    1 kg carrot
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.WARNING, "cannot resolve carrot") }
        verify { mock.builder.range(element.inputProductSpec) }
        verify { mock.builder.highlightType(ProblemHighlightType.WARNING) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFound_wrongDim_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_whenFound_wrongDim_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    1 l carrot
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
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.ERROR, "incompatible dimensions: length³ vs mass") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_withFromProcess_wrongDim_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_withFromProcess_wrongDim_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    1 kg carrot from carrot_prod(x = 1 l)
                }
            }
            
            process carrot_prod {
                params {
                    x = 1 kg
                }
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "incompatible types: expecting TQuantity(dimension=mass), found TQuantity(dimension=length³)"
            )
        }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_withFromProcess_unknownParameter_shouldAnnotate() {
        // given
        val pkgName = "testAnnotate_withFromProcess_unknownParameter_shouldAnnotate"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    1 kg carrot from carrot_prod(y = 1 l)
                }
            }
            
            process carrot_prod {
                params {
                    x = 1 kg
                }
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()


        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.ERROR, "unknown parameter y") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenFound_shouldDoNothing() {
        val pkgName = "testAnnotate_whenFound_shouldDoNothing"
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    1 kg carrot
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
        val mock = AnnotationHolderMock()
        val annotator = LcaTechnoInputExchangeAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
