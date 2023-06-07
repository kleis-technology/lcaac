package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.openapi.ui.naturalSorted
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class DefaultRefFileResolverTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_shouldLoadFilesAccordingToImportStatements() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                import $pkgName.right
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production match (dir="left")
                    }
                }
            """.trimIndent()
        )
        val target = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec
            .getProcessTemplateSpec()!!
        myFixture.createFile(
            "$pkgName.left.lca", """
                package $pkgName.left
                process carrot_production {
                    labels {
                        dir="left"
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.right.lca", """
                package $pkgName.right
                process carrot_production {
                    labels {
                        dir="right"
                    }
                }
            """.trimIndent()
        )
        val resolver = DefaultRefFileResolver(project)

        // when
        val actual = resolver.resolve(target)
            .map { it.virtualFile.name }
            .naturalSorted()

        // then
        val expected = listOf("$pkgName.right.lca").naturalSorted()
        assertEquals(expected, actual)
    }
}
