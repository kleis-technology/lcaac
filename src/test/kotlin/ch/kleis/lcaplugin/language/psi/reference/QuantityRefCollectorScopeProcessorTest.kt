package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.psi.LcaQuantityRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.ui.naturalSorted
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QuantityRefCollectorScopeProcessorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_quantityRefCollectorScopeProcessor_getVariants() {
        // given
        val pkgName = "test_quantityRefCollectorScopeProcessor_getVariants"
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                import $pkgName.bis
                
                variables {
                    a = 1 kg
                }
                
                process p {
                    params {
                        b = 2 kg
                    }
                    variables {
                        c = 3 kg
                    }
                    inputs {
                        a foo
                    }
                }
            """.trimIndent()
        )

        myFixture.createFile(
            "$pkgName.bis.lca", """
                package $pkgName.bis
                
                variables {
                    d = 2 kg
                }
            """.trimIndent()
        )
        val process = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val target = process.getInputs().first()
            .getQuantity() as LcaQuantityRef

        // when
        val actual = target.reference.variants
            .map { (it as LookupElementBuilder).lookupString }
            .naturalSorted()

        // then
        val expected = listOf("a", "b", "c", "d").naturalSorted()
        assertEquals(expected, actual)
    }
}
