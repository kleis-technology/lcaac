package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.ui.naturalSorted
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SankeyGraphBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    private data class SankeyRequiredInformation(
        val observedPort: MatrixColumnIndex,
        val allocatedSystem: SystemValue,
        val inventory: Inventory,
        val comparator: Comparator<MatrixColumnIndex>,
    )

    private fun getRequiredInformation(@Suppress("SameParameterValue") process: String, vf: VirtualFile): SankeyRequiredInformation {
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(symbolTable.getTemplate(process)!!, emptyMap())
        val trace = Evaluator(symbolTable).trace(entryPoint)
        val assessment = Assessment(trace.getSystemValue(), trace.getEntryPoint())
        val inventory = assessment.inventory()
        val allocatedSystem = assessment.allocatedSystem
        val sankeyPort = inventory.getControllablePorts().getElements().first()
        return SankeyRequiredInformation(sankeyPort, allocatedSystem, inventory, trace.getObservableOrder())
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
                           emissions{
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenRessource_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           resources {
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Resource] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Resource] my_substance(air)", 1.0),
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
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_input", "my_input"),
            GraphNode("my_product from p{}{}", "my_product"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenTwoLinks_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process p {
                    products {
                        1 kg prod
                    }
                    inputs {
                        2 kg qrod
                    }
                }
                process q {
                    products {
                        1 kg qrod
                    }
                    emissions {
                        2.5 kg my_emission
                    }
               }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("prod from p{}{}", "prod"),
            GraphNode("qrod from q{}{}", "qrod"),
            GraphNode("my_emission", "my_emission"),
        ).addLink(
            GraphLink("prod from p{}{}", "qrod from q{}{}", 5.0),
            GraphLink("qrod from q{}{}", "my_emission", 5.0),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
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
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("[Emission] my_substance(air)", "my_substance")
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
                               50 percent * 1 kg my_input
                           }
                       }
                       process input {
                           products {
                                1 kg my_input
                           }
                           inputs {
                               1000 g my_indicator
                           }
                      }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_input from input{}{}", "my_input"),
            GraphNode("my_indicator", "my_indicator"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input from input{}{}", 500.0),
            GraphLink("my_input from input{}{}", "my_indicator", 500.0),
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
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_other_product from p{}{}", "my_other_product"),
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
            GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenDiamondShaped_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                    process p {
                        products {
                            1 kg my_product
                        }
                        inputs {
                            1 kg my_left_product
                            1 kg my_right_product
                        }
                    }
                    process q {
                        products {
                            1 kg my_left_product
                        }
                        inputs {
                            1 kg my_input
                        }
                    }
                    process r {
                        products {
                            1 kg my_right_product
                        }
                        inputs {
                            1 kg my_input
                        }
                    }
                    process input {
                        products {
                            1 kg my_input
                        }
                        emissions {
                            1 kg my_substance
                        }
                    }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_substance", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_left_product from q{}{}", "my_left_product"),
            GraphNode("my_right_product from r{}{}", "my_right_product"),
            GraphNode("my_input from input{}{}", "my_input"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_left_product from q{}{}", 1.0),
            GraphLink("my_product from p{}{}", "my_right_product from r{}{}", 1.0),
            GraphLink("my_left_product from q{}{}", "my_input from input{}{}", 1.0),
            GraphLink("my_right_product from r{}{}", "my_input from input{}{}", 1.0),
            GraphLink("my_input from input{}{}", "my_substance", 2.0),
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
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_input from q{}{}", "my_input"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_other_product from p{}{}", "my_other_product"),
            GraphNode("[Emission] my_substance(air)", "my_substance")
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

    @Test
    fun test_whenCycle_thenAcyclicGraph() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process p1 {
                    products {
                        1 kg A
                    }
                    inputs {
                        1 kg B
                    }
                }
                process p2 {
                    products {
                        1 kg B
                    }
                    inputs {
                        0.5 kg A
                    }
                    emissions {
                        1 kg C
                    }
                }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p1", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("A from p1{}{}", "A"),
            GraphNode("B from p2{}{}", "B"),
            GraphNode("C", "C"),
        ).addLink(
            GraphLink("A from p1{}{}", "B from p2{}{}", 4.0),
            GraphLink("B from p2{}{}", "C", 2.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenComplexCycles_thenAcyclicSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process pA {
                    products {
                        1 kg A
                    }
                    inputs {
                        0.5 kg B
                        0.5 kg C
                    }
                }

                process pB {
                    products {
                        1 kg B
                    }
                    inputs {
                        0.5 kg B
                        1 kg D
                        1 kg E
                    }
                }

                process pC {
                    products {
                        1 kg C
                    }
                    inputs {
                        1 kg F
                    }
                }

                process pD {
                    products {
                        1 kg D
                    }
                    inputs {
                        1 kg F
                    }
                }

                process pE {
                    products {
                        1 kg E
                    }
                    inputs {
                        1 kg G
                    }
                }

                process pF {
                    products {
                        1 kg F
                    }
                    inputs {
                        1 kg H
                    }
                }

                process pG {
                    products {
                        1 kg G
                    }
                    inputs {
                        0.5 kg E
                        1 kg H
                    }
                }

                process pH {
                    products {
                        1 kg H
                    }
                    emissions {
                        1 kg my_emission
                    }
                } 
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("pA", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("A from pA{}{}", "A"),
            GraphNode("B from pB{}{}", "B"),
            GraphNode("C from pC{}{}", "C"),
            GraphNode("D from pD{}{}", "D"),
            GraphNode("E from pE{}{}", "E"),
            GraphNode("F from pF{}{}", "F"),
            GraphNode("G from pG{}{}", "G"),
            GraphNode("H from pH{}{}", "H"),
            GraphNode("my_emission", "my_emission")
        ).addLink(
            GraphLink("F from pF{}{}", "H from pH{}{}", value = 1.5),
            GraphLink("C from pC{}{}", "F from pF{}{}", value = 0.5),
            GraphLink("A from pA{}{}", "B from pB{}{}", value = 3.0),
            GraphLink("A from pA{}{}", "C from pC{}{}", value = 0.5),
            GraphLink("D from pD{}{}", "F from pF{}{}", value = 1.0),
            GraphLink("H from pH{}{}", "my_emission", value = 3.5),
            GraphLink("B from pB{}{}", "D from pD{}{}", value = 1.0),
            GraphLink("B from pB{}{}", "E from pE{}{}", value = 2.0),
            GraphLink("E from pE{}{}", "G from pG{}{}", value = 4.0),
            GraphLink("G from pG{}{}", "H from pH{}{}", value = 2.0),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }
}