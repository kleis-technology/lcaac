package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.psi.LcaDataRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.ui.naturalSorted
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataRefCollectorScopeProcessorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_dataRefCollectorScopeProcessor_getVariants() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
                    labels {
                        d = "LABEL"
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
                    e = 2 kg
                }
            """.trimIndent()
        )
        val process = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p", mapOf("d" to "LABEL")).first()
        val target = process.getInputs().first()
            .dataExpression as LcaDataRef

        // when
        val actual = target.reference.variants
            .map { (it as LookupElementBuilder).lookupString }
            .naturalSorted()

        // then
        assertContainsElements(actual, "a", "b", "c", "d", "e")
    }
}
