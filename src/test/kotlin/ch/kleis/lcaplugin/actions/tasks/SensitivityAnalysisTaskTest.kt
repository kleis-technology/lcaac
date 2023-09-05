package ch.kleis.lcaplugin.actions.tasks

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.impl.AnyModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SensitivityAnalysisTaskTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    private class MockProgressIndicator : ProgressIndicator {
        override fun start() {
        }

        override fun stop() {
        }

        override fun isRunning(): Boolean {
            return true
        }

        override fun cancel() {
        }

        override fun isCanceled(): Boolean {
            return false
        }

        override fun setText(text: String?) {
        }

        override fun getText(): String {
            return ""
        }

        override fun setText2(text: String?) {
        }

        override fun getText2(): String {
            return ""
        }

        override fun getFraction(): Double {
            return 1.0
        }

        override fun setFraction(fraction: Double) {
        }

        override fun pushState() {
        }

        override fun popState() {
        }

        override fun isModal(): Boolean {
            return false
        }

        override fun getModalityState(): ModalityState {
            return AnyModalityState.ANY
        }

        override fun setModalityProgress(modalityProgress: ProgressIndicator?) {
        }

        override fun isIndeterminate(): Boolean {
            return true
        }

        override fun setIndeterminate(indeterminate: Boolean) {
        }

        override fun checkCanceled() {
        }

        override fun isPopupWasShown(): Boolean {
            return false
        }

        override fun isShowing(): Boolean {
            return true
        }
    }

    @Test
    fun test_sensitivity_basic() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        q = 2 kg
                    }
                    products {
                        1 kg carrot
                    }
                    impacts {
                        q cc
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val task = SensitivityAnalysisTask(project, file, "p", emptyMap())
        val indicator = MockProgressIndicator()

        // when
        task.run(indicator)
        val analysis = task.getAnalysis()!!
        val actual = analysis.getRelativeSensibility(
            analysis.getObservablePorts()[0],
            analysis.getControllablePorts()[0],
            analysis.getParameters().getName(0),
        )

        // then
        assertEquals(1.0, actual)
    }

    @Test
    fun test_sensitivity_cube() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        q = 2 kg
                    }
                    products {
                        1 kg carrot
                    }
                    impacts {
                        q^3 cc
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val task = SensitivityAnalysisTask(project, file, "p", emptyMap())
        val indicator = MockProgressIndicator()

        // when
        task.run(indicator)
        val analysis = task.getAnalysis()!!
        val actual = analysis.getRelativeSensibility(
            analysis.getObservablePorts()[0],
            analysis.getControllablePorts()[0],
            analysis.getParameters().getName(0),
        )

        // then
        assertEquals(3.0, actual)
    }
}
