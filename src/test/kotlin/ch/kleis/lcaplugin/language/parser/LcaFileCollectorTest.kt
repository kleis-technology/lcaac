package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.ui.naturalSorted
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaFileCollectorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_collect_whenPatternMatch() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        dir = "right"
                    }
                    inputs {
                        1 kg carrot from carrot_production match (dir = dir)
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val vfRight = myFixture.createFile(
            "$pkgName.right.lca", """
                package $pkgName
                
                process carrot_production {
                    labels {
                        dir = "right"
                    }
                    products {
                        1 kg carrot
                    }
                }
            """.trimIndent()
        )
        val right = PsiManager.getInstance(project).findFile(vfRight) as LcaFile
        val vfLeft = myFixture.createFile(
            "$pkgName.left.lca", """
                package $pkgName
                
                process carrot_production {
                    labels {
                        dir = "left"
                    }
                    products {
                        1 kg carrot
                    }
                }
            """.trimIndent()
        )
        val left = PsiManager.getInstance(project).findFile(vfLeft) as LcaFile
        val collector = LcaFileCollector(project)

        // when
        val actual = collector.collect(file)
            .map { it.name }
            .toList().naturalSorted()

        val expected =
            listOf(UnitFixture.getInternalUnitFile(myFixture), file, left, right).map { it.name }.naturalSorted()
        assertEquals(expected, actual)
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
        val collector = LcaFileCollector(project)

        // when
        val actual = collector.collect(fa)
            .map { it.virtualFile.path }
            .toList().naturalSorted()

        // then
        val expected = listOf(fa, fb)
            .map { it.virtualFile.path }
            .naturalSorted()
        // Have to contains Units, whatever is located
        assertEquals(1, actual.filter { it.endsWith("built_in_units.lca") }.size)

        // And the other file
        assertEquals(expected, actual.filter { !it.endsWith("built_in_units.lca") })
    }
}
