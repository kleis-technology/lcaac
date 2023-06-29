package ch.kleis.lcaplugin.graph

import ch.kleis.lcaplugin.actions.SankeyGraphAction
import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.*
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SankeyGraphTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    private data class SankeyRequiredInformation(val port: MatrixColumnIndex, val trace: EvaluationTrace, val inventory: Inventory)

    private fun getRequiredInformation(process: String, vf: VirtualFile): SankeyRequiredInformation {
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(symbolTable.getTemplate(process)!!, emptyMap())
        val trace = Evaluator(symbolTable).trace(entryPoint)
        val assessment = Assessment(trace.getSystemValue(), trace.getEntryPoint())
        val inventory = assessment.inventory()
        val sankeyPort = inventory.getControllablePorts().getElements().first()
        return SankeyRequiredInformation(sankeyPort, trace, inventory)
    }

    @Test
    fun test_whenEmission_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           emissions {
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "[Emission] my_substance(air)"),
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenProducts_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process p {
                    products {
                        1 kg my_product
                    }
                    inputs {
                        1 kg my_input
                    }
                }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_input", "my_input"),
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenSubstanceImpacts_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                substance my_substance {
                    name = "my_substance"
                    type = Emission
                    compartment = "air"
                    reference_unit = m3
                    impacts {
                        1 u climate_change
                    }
                }
                process p {
                    products {
                        1 kg my_product
                    }
                    emissions {
                        1 m3 my_substance (compartment = "air")
                    }
                }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
            GraphNode("[Emission] my_substance(air)", "[Emission] my_substance(air)")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
            GraphLink("[Emission] my_substance(air)", "climate_change", 1.0)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)

    }

    @Test
    fun test_units() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           inputs {
                               500 g my_input
                           }
                       }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
            GraphNode("my_input", "my_input"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input", 0.5),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenAllocation_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product allocate 50 percent
                               1 kg my_other_product allocate 50 percent
                           }
                           emissions {
                               2 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "[Emission] my_substance(air)"),
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
            GraphNode("my_other_product from p{}{}", "my_other_product from p{}{}"),
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
            GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenComplexGraph_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       substance my_substance {
                           name = "my_substance"
                           type = Emission
                           compartment = "air"
                           reference_unit = m3
                           impacts {
                               1 u climate_change
                           }
                       }
                       
                       process p {
                           products {
                               1 kg my_product allocate 75 percent
                               1 kg my_other_product allocate 25 percent
                           }
                           inputs {
                               2 kg my_input
                           }
                           emissions {
                               1 m3 my_substance (compartment = "air")
                           }
                       }

                       process q {
                           products {
                               1 kg my_input
                           }
                           emissions {
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent())
        val (sankeyPort, trace, inventory) = getRequiredInformation("p", vf)
        val action = SankeyGraphAction("p", mapOf())

        // when
        val graph = action.buildContributionGraph(sankeyPort, trace, inventory)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_input from q{}{}", "my_input from q{}{}"),
            GraphNode("my_product from p{}{}", "my_product from p{}{}"),
            GraphNode("my_other_product from p{}{}", "my_other_product from p{}{}"),
            GraphNode("[Emission] my_substance(air)", "[Emission] my_substance(air)")
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input from q{}{}", 1.5),
            GraphLink("my_other_product from p{}{}", "my_input from q{}{}", 0.5),
            GraphLink("my_input from q{}{}", "[Emission] my_substance(air)", 2.0),
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 0.75),
            GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 0.25),
            GraphLink("[Emission] my_substance(air)", "climate_change", 3.0)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }
}