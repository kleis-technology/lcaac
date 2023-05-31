package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class ParameterReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_variants() {
        // given
        val pkgName = "test_variants"
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_prod( s = 1 m2 )
                    }
                }

                process water_prod {
                    params {
                        size = 1 m2
                        weight = 1 m2
                        density = 1 m2
                    }

                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val fqn = "$pkgName.p"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val ref = process.getInputs().first()
            .inputProductSpec
            .getFromProcessConstraint()
            ?.processTemplateSpec!!
            .argumentList.first()
            .parameterRef

        // when
        val actual =
            ref.reference
                .variants
                .map { (it as LookupElementBuilder).lookupString }
                .sorted()

        // then
        val expected = listOf("size", "weight", "density").sorted()
        TestCase.assertEquals(expected, actual.sorted())
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = "test_resolve"
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_prod( size = 1 m2 )
                    }
                }

                process water_prod {
                    params {
                        size = 1 m2
                    }

                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val fqn = "$pkgName.p"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val ref = process.getInputs().first()
            .inputProductSpec
            .getFromProcessConstraint()
            ?.processTemplateSpec!!
            .argumentList.first()
            .parameterRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.water_prod").first()
            .getLcaParams().first()
            .assignmentList.first()
        TestCase.assertEquals(expected, actual)
    }
}
