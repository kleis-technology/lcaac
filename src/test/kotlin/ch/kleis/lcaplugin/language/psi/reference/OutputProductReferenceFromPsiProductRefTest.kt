package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OutputProductReferenceFromPsiProductRefTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_whenInsideInputProductSpec() {
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
        val ref = ProcessStubKeyIndex
            .findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec
            .getProductRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex
            .findProcesses(project, "$pkgName.carrot_production").first()
            .getProducts().first()
            .outputProductSpec
        assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenInsideOutputProductSpec() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process carrot_production {
                    products {
                        1 kg carrot
                    }
                }
            """.trimIndent()
        )
        val outputProductSpec = ProcessStubKeyIndex
            .findProcesses(project, "$pkgName.carrot_production").first()
            .getProducts().first()
            .outputProductSpec
        val ref = outputProductSpec
            .getProductRef()

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(outputProductSpec, actual)
    }
}
