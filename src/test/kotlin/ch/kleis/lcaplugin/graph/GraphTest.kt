package ch.kleis.lcaplugin.graph

import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
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
        val n1 = GraphNode("beer", "beer")
        val n2 = GraphNode("whisky", "whisky")
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
        val n = GraphNode("beer", "beer")

        // When
        sut.addNode(n)

        // Then
        assertThat(sut.nodes, not(hasItem(n)))
    }

    @Test
    fun addLink_ShouldAppendLink() {
        // Given
        val l1 = GraphLink("beer", "drinking", 1.0)
        val l2 = GraphLink("whisky", "drinking", 3.0)
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
        val l = GraphLink("beer", "drinking", 1.0)

        // When
        sut.addLink(l)

        // Then
        assertThat(sut.links, not(hasItem(l)))
    }

    @Test
    fun merge_ShouldUnionGraphs_WhenOneArgument() {
        // Given
        val beer = GraphNode("beer", "beer")
        val drinkingB = GraphNode("drinking", "drinking")
        val beerDrinking = GraphLink("beer", "drinking", 1.0)

        val gin = GraphNode("gin", "gin")
        val drinkingG = GraphNode("drinking", "drinking")
        val ginDrinking = GraphLink("gin", "drinking", 1.0)

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
        val beer = GraphNode("beer", "beer")
        val drinkingB = GraphNode("drinking", "drinking")
        val beerDrinking = GraphLink("beer", "drinking", 1.0)
        val graphBeer = Graph(setOf(beer, drinkingB), setOf(beerDrinking))

        val gin = GraphNode("gin", "gin")
        val drinkingG = GraphNode("drinking", "drinking")
        val ginDrinking = GraphLink("gin", "drinking", 1.0)
        val graphGin = Graph(setOf(gin, drinkingG), setOf(ginDrinking))

        val aspirin = GraphNode("aspirin", "aspirin")
        val eating = GraphNode("eating", "eating")
        val eatingAspirin = GraphLink("aspirin", "eating", 100.0)
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
    fun graphNode_ShouldBeInjective() {
        // Given
        val n1 = GraphNode("foo", "pretty_foo")
        val n2 = GraphNode("bar", "pretty_bar")
        val n3 = GraphNode("baz", "pretty_baz")

        // Then
        assertNotEquals(n1, n2)
        assertNotEquals(n2, n3)
        assertNotEquals(n1, n3)
    }

    @Test
    fun graphLink_ShouldBeInjective() {
        // Given
        val l1 = GraphLink("foo", "bar", 1.0)
        val l2 = GraphLink("baz", "floob", 2.0)

        // Then
        assertNotEquals(l1, l2)
    }
}