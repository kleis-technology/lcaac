package ch.kleis.lcaplugin.graph

import ch.kleis.lcaplugin.core.graph.*
import ch.kleis.lcaplugin.core.lang.fixture.BioExchangeValueFixture.Companion.propanolBioExchange
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GraphTest {
    @Test
    fun addNode_ShouldAppendNode() {
        // Given
        val n1 = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")
        val n2 = GraphNode("whisky", GraphNodeType.SUBSTANCE, "whisky")
        val g = Graph(setOf(n1), setOf())

        // When
        val sut = g.addNode(n2)

        // Then
        assertThat(sut.nodes, hasItem(n1))
        assertThat(sut.nodes, hasItem(n2))
    }

    @Test
    fun addNode_ShouldNotViolateGraphImmutability() {
        // Given
        val sut = Graph.empty()
        val n = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")

        // When
        sut.addNode(n)

        // Then
        assertThat(sut.nodes, not(hasItem(n)))
    }

    @Test
    fun addLink_ShouldAppendLink() {
        // Given
        val l1 = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")
        val l2 = GraphLink("whisky", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "10cL")
        val g = Graph(setOf(), setOf(l1))

        // When
        val sut = g.addLink(l2)

        // Then
        assertThat(sut.links, hasItem(l1))
        assertThat(sut.links, hasItem(l2))
    }

    @Test
    fun addLink_ShouldNotViolateGraphImmutability() {
        // Given
        val sut = Graph.empty()
        val l = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")

        // When
        sut.addLink(l)

        // Then
        assertThat(sut.links, not(hasItem(l)))
    }

    @Test
    fun merge_ShouldUnionGraphs_WhenOneArgument() {
        // Given
        val beer = GraphNode("beer", GraphNodeType.SUBSTANCE, "beer")
        val drinkingB = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val beerDrinking = GraphLink("beer", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1L")

        val gin = GraphNode("gin", GraphNodeType.SUBSTANCE, "gin")
        val drinkingG = GraphNode("drinking", GraphNodeType.PROCESS, "drinking")
        val ginDrinking = GraphLink("gin", "drinking", GraphLinkType.BIOSPHERE_EXCHANGE, "1cL")

        val sut = Graph(setOf(gin, drinkingG), setOf(ginDrinking))
        val beerGraph = Graph(setOf(beer, drinkingB), setOf(beerDrinking))

        // When
        val resultingGraph = sut.merge(beerGraph)

        // Then
        assertEquals(
            Graph(setOf(beer, gin, drinkingG), setOf(beerDrinking, ginDrinking)),
            resultingGraph
        )
    }

    @Test
    fun merge_ShouldUnionGraphs_WhenVarArgs() {
        // Given
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

        // When
        val sut = Graph.empty()
        val result = sut.merge(graphGin, graphBeer, graphAspirin)

        // Then
        assertEquals(
            Graph(setOf(beer, gin, aspirin, drinkingG, eating), setOf(beerDrinking, ginDrinking, eatingAspirin)),
            result
        )

        assertEquals(
            Graph.empty().merge(graphBeer).merge(graphGin).merge(graphAspirin),
            result
        )
    }

    @Test
    fun graphNode_ShouldBePureFunction_WhenCalledWithProcessName() {
        // Given
        val processName = "foo"

        // When
        val n1 = GraphNode(processName)
        val n2 = GraphNode(processName)

        // Then
        assertEquals(n1, n2)
    }

    @Test
    fun graphNode_ShouldBePureFunction_WhenCalledWithTechnoExchange() {
        // Given
        val technoExchange = carrotTechnoExchangeValue

        // When
        val n1 = GraphNode(technoExchange)
        val n2 = GraphNode(technoExchange)

        // Then
        assertEquals(n1, n2)
    }

    @Test
    fun graphNode_ShouldBePureFunction_WhenCalledWithBioExchange() {
        // Given
        val bioExchange = propanolBioExchange

        // When
        val n1 = GraphNode(bioExchange)
        val n2 = GraphNode(bioExchange)

        // Then
        assertEquals(n1, n2)
    }

    @Test
    fun graphNode_ShouldBeInjective() {
        // Given
        val technoExchange = carrotTechnoExchangeValue
        val bioExchange = propanolBioExchange
        val processName = "foo"

        // When
        val n1 = GraphNode(technoExchange)
        val n2 = GraphNode(bioExchange)
        val n3 = GraphNode(processName)

        // Then
        assertNotEquals(n1, n2)
        assertNotEquals(n2, n3)
        assertNotEquals(n1, n3)
    }

    @Test
    fun graphLink_ShouldBePureFunction_WhenCalledWithTechnoExchange() {
        // Given
        val technoExchange = carrotTechnoExchangeValue
        val isInput = false
        val processName = "foo"

        // When
        val l1 = GraphLink(isInput, processName, technoExchange)
        val l2 = GraphLink(isInput, processName, technoExchange)

        // Then
        assertEquals(l1, l2)
    }

    @Test
    fun graphLink_ShouldBePureFunction_WhenCalledWithBioExchange() {
        // Given
        val bioExchange = propanolBioExchange
        val processName = "foo"

        // When
        val l1 = GraphLink(processName, bioExchange)
        val l2 = GraphLink(processName, bioExchange)

        // Then
        assertEquals(l1, l2)
    }

    @Test
    fun graphLink_ShouldBeInjective() {
        // Given
        val technoExchange = carrotTechnoExchangeValue
        val bioExchange = propanolBioExchange
        val processName = "foo"
        val isInput = false

        // When
        val l1 = GraphLink(isInput, processName, technoExchange)
        val l2 = GraphLink(processName, bioExchange)

        // Then
        assertNotEquals(l1, l2)
    }
}