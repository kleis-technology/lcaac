package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.output_product.OutputProductStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test


class OutputProductReferenceFromPsiInputProductSpecTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_withDataRefInMatchLabelExpression() {
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
                    params {
                        geo = "FR"
                    }
                    inputs {
                        1 l water from water_production match (geo = geo)
                    }
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.water.lca", """
                package $pkgName.water
                
                process water_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 l water
                    }
                }
                
                process water_production {
                    labels {
                        geo = "UK"
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
            mapOf("geo" to "FR"),
        ).first()
            .getProducts().first()
            .outputProductSpec
        assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_productNameOnly_ambiguousProcess() {
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
        Assert.assertNull(actual)
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
        val expected = OutputProductStubKeyIndex.findOutputProducts(project, "$pkgName.water.water").first()
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
