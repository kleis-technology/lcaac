package ch.kleis.lcaplugin.language.psi.index

import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.ui.naturalSorted
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LcaProcessFileIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_findFiles() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf1 = myFixture.createFile(
            "$pkgName.1.lca", """
                package $pkgName.a1
                
                process p {
                    labels {
                        a = "1"
                    }
                }
            """.trimIndent()
        )
        val lcaFile1 = PsiManager.getInstance(project).findFile(vf1) as LcaFile
        val vf2 = myFixture.createFile(
            "$pkgName.2.lca", """
                package $pkgName.a2
                
                process p {
                    labels {
                        a = "2"
                    }
                }
            """.trimIndent()
        )
        val lcaFile2 = PsiManager.getInstance(project).findFile(vf2) as LcaFile
        myFixture.createFile(
            "$pkgName.3.lca", """
                package $pkgName.a3
                
                process q {
                }
            """.trimIndent()
        )

        // when
        val actual = LcaProcessFileIndex.findFiles(project, "p")
            .map { it.name }
            .naturalSorted()

        // then
        val expected = listOf(lcaFile1, lcaFile2)
            .map { it.name }
            .naturalSorted()
        assertEquals(expected, actual)
    }
}
