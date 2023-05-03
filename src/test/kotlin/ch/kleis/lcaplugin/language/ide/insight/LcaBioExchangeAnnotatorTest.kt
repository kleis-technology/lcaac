package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test

class LcaBioExchangeAnnotatorTest : BasePlatformTestCase() {

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
                emissions {
                    1 kg co2(compartment = "air")
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
        val mock = AnnotationHolderMock()
        val annotator = LcaBioExchangeAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.WARNING,
                """unresolved substance co2(compartment="air")"""
            )
        }
        verify { mock.builder.range(element.getSubstanceSpec()) }
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
                emissions {
                    1 kg co2(compartment="compartment")
                }
            }
            
            substance co2 {
                name = "co2"
                type = Emission
                compartment = "compartment"
                reference_unit = kg
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first()
        val mock = AnnotationHolderMock()
        val annotator = LcaBioExchangeAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
