package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
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

    @Test
    fun test_getVariants() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                import $pkgName.carrot
                import $pkgName.salad

                process p {
                    products {
                        1 kg a
                    }
                    inputs {
                        1 kg carrot from carrot_production()
                    }
                }
                
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.carrot.lca", """
                package $pkgName.carrot

                process carrot_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 kg carrot
                    }
                    emissions {
                        1 kg climate_change
                    }
                }
                
                process carrot_production {
                    labels {
                        geo = "UK"
                    }
                    products {
                        1 kg carrot
                    }
                    emissions {
                        2 kg climate_change
                    }
                }
                
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.salad.lca", """
                package $pkgName.salad
                
                process salad_production {
                    products {
                        1 kg salad
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
        val actual =
            ref.reference
                .variants
                .map { (it as LookupElementBuilder).lookupString }
                .sorted()


        // then
        val expected = listOf(
            "salad_production", "p",
            "carrot_production match (geo = \"FR\")",
            "carrot_production match (geo = \"UK\")",
        ).sorted()
        TestCase.assertEquals(expected, actual.sorted())
    }
}
