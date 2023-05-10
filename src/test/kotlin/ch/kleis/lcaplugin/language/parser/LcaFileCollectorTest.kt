package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.ui.naturalSorted
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LcaFileCollectorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testCollect_shouldCollectAllFilesResolved() {
        // given
        val vfa = myFixture.createFile(
            "fa.lca", """
            package ch.kleis.pkg_a
            
            import ch.kleis.pkg_b
            
            process a {
                products {
                    1 kg a
                }
                emissions {
                    1 kg b(compartment = "compartment")
                }
            }
        """.trimIndent()
        )
        val fa = PsiManager.getInstance(project).findFile(vfa) as LcaFile
        val vfb = myFixture.createFile(
            "fb.lca", """
            package ch.kleis.pkg_b
            
            substance b {
                name = "b"
                type = Emission
                compartment = "compartment"
                reference_unit = kg
            }
        """.trimIndent()
        )
        val fb = PsiManager.getInstance(project).findFile(vfb) as LcaFile
        val collector = LcaFileCollector()

        // when
        val actual = collector.collect(fa)
            .map { it.virtualFile.path }
            .toList().naturalSorted()

        // then
        val expected = listOf(fa, fb)
            .map { it.virtualFile.path }
            .naturalSorted()
        assertEquals(expected, actual)
    }
}
