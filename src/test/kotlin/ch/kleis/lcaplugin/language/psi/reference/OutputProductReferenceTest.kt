package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Test


class OutputProductReferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_fromProcess_withPatternMatching_noLabels() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.water

                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName.water
                
                process water_production {
                    products {
                        1 l water
                    }
                }
                
                process water_production {
                    labels {
                        geo = "2"
                    }
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.water.water_production",
            emptyMap(),
        ).first()
            .getProducts().first()
            .outputProductSpec
        assertEquals(expected, actual)
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.water

                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName.water
                
                process water_production {
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = OutputProductKeyIndex.findOutputProducts(project, "$pkgName.water.water").first()
        assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_fromProcess_sameProduct() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.water

                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production_1
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName.water
                
                process water_production_1 {
                    products {
                        1 l water
                    }
                }
                
                process water_production_2 {
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.water.water_production_1").first()
            .getProducts().first()
            .outputProductSpec
        assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_fromProcess_withPatternMatching() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.water

                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production match (geo = "1")
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName.water
                
                process water_production {
                    labels {
                        geo = "1"
                    }
                    products {
                        1 l water
                    }
                }
                
                process water_production {
                    labels {
                        geo = "2"
                    }
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.water.water_production",
            mapOf("geo" to "1"),
        ).first()
            .getProducts().first()
            .outputProductSpec
        assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.water

                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName
                
                process water_production {
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .inputProductSpec

        // when
        val actual = ref.reference.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf("carrot", "water")
        TestCase.assertEquals(expected, actual)

        // clean
        unmockkStatic(StubIndex::class)
    }
}
