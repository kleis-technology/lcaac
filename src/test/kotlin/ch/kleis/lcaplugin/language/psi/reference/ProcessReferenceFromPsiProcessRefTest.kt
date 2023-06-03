package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class ProcessReferenceFromPsiProcessRefTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_whenDefiningProcess() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                }
            """.trimIndent()
        )
        val process = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val ref = process.getProcessRef()

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(process, actual)
    }

    @Test
    fun test_resolve_whenInsideProcessTemplateSpec() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production
                    }
                }
                
                process carrot_production {
                    products {
                        1 kg carrot
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec
            .getProcessTemplateSpec()!!
            .getProcessRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.carrot_production").first()
        assertEquals(expected, actual)
    }
}
