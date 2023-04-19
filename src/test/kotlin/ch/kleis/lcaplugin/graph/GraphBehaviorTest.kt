package ch.kleis.lcaplugin.graph

import ch.kleis.lcaplugin.core.graph.*
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import junit.framework.TestCase
import org.junit.Test

class GraphBehaviorTest {
    @Test
    fun testAddNode() {
        val g1 = Graph.empty()
        val n = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")
        TestCase.assertTrue(g1.addNode(n).nodes.contains(n))
    }

    @Test
    fun testAddLink() {
        val g1 = Graph.empty()
        val l = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")
        TestCase.assertTrue(g1.addLink(l).links.contains(l))
    }

    @Test
    fun testMerge() {
        val beer = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")
        val drinkingB = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val beerDrinking = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")
        val graphBeer = Graph(setOf(beer, drinkingB), setOf(beerDrinking))

        val gin = GraphNode("gin", GraphNodeType.SUBSTANCE, "gin")
        val drinkingG = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val ginDrinking = GraphLink("gin", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1cL")
        val graphGin = Graph(setOf(gin, drinkingG), setOf(ginDrinking))

        // Note only one of the drinking process nodes
        TestCase.assertEquals(
            graphGin.merge(graphBeer),
            Graph(
                setOf(beer, gin, drinkingG), setOf(
                    beerDrinking,
                    ginDrinking
                )
            )
        )
    }

    @Test
    fun testMergeMany() {
        val beer = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")
        val drinkingB = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val beerDrinking = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")
        val graphBeer = Graph(setOf(beer, drinkingB), setOf(beerDrinking))

        val gin = GraphNode("gin", GraphNodeType.SUBSTANCE, "gin")
        val drinkingG = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val ginDrinking = GraphLink("gin", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1cL")
        val graphGin = Graph(setOf(gin, drinkingG), setOf(ginDrinking))

        val aspirin = GraphNode("aspirin", GraphNodeType.PRODUCT, "aspirin")
        val eating = GraphNode("eating", GraphNodeType.PROCESS, "eating")
        val eatingAspirin = GraphLink("aspirin", "eating", GraphLinkType.BIOSPHERE_EXCHANGE, "100mg")
        val graphAspirin = Graph(setOf(aspirin, eating), setOf(eatingAspirin))

        // Note only one of the drinking process nodes
        TestCase.assertEquals(
            graphGin.merge(graphBeer, graphAspirin),
            Graph(
                setOf(beer, gin, aspirin, drinkingG, eating), setOf(
                    beerDrinking,
                    ginDrinking,
                    eatingAspirin
                )
            )
        )

        TestCase.assertEquals(
            Graph.empty().merge(graphBeer).merge(graphGin).merge(graphAspirin),
            Graph.empty().merge(graphBeer, graphGin, graphAspirin)
        )
    }

    @Test
    fun addTechnoExchangeTwice() {
        TestCase.assertEquals(Graph.empty().addNode(GraphNode(carrotTechnoExchangeValue)).addNode(GraphNode(carrotTechnoExchangeValue)),
            Graph(setOf(GraphNode(carrotTechnoExchangeValue)), setOf()))
    }
}