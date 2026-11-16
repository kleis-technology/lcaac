package ch.kleis.lcaac.core.graph

import kotlin.test.Test
import kotlin.test.assertEquals

class StronglyConnectedComponentsTest {
    private fun components(edges: Map<Int, List<Int>>): Set<Set<Int>> =
        stronglyConnectedComponents(edges.keys) { edges[it] ?: emptyList() }.map { it.toSet() }.toSet()

    @Test
    fun chain_shouldHaveOneComponentPerNode() {
        assertEquals(setOf(setOf(1), setOf(2), setOf(3)), components(mapOf(1 to listOf(2), 2 to listOf(3), 3 to emptyList())))
    }

    @Test
    fun cycle_shouldBeOneComponent() {
        assertEquals(setOf(setOf(1, 2, 3)), components(mapOf(1 to listOf(2), 2 to listOf(3), 3 to listOf(1))))
    }

    @Test
    fun selfLoop_shouldBeItsOwnComponent() {
        assertEquals(setOf(setOf(1), setOf(2)), components(mapOf(1 to listOf(1, 2), 2 to emptyList())))
    }

    @Test
    fun cyclesLinkedByAChain_shouldBeSeparateComponents() {
        // 1 <-> 2 -> 3 -> 4 <-> 5, and 3 alone in between
        val edges = mapOf(1 to listOf(2), 2 to listOf(1, 3), 3 to listOf(4), 4 to listOf(5), 5 to listOf(4))
        assertEquals(setOf(setOf(1, 2), setOf(3), setOf(4, 5)), components(edges))
    }

    @Test
    fun disconnectedGraphs_shouldAllBeVisited() {
        val edges = mapOf(1 to listOf(2), 2 to listOf(1), 3 to listOf(4), 4 to emptyList())
        assertEquals(setOf(setOf(1, 2), setOf(3), setOf(4)), components(edges))
    }

    @Test
    fun nodesReachedOnlyThroughEdges_shouldBeIncluded() {
        // 3 is not in the list of nodes, but 2 points to it
        assertEquals(setOf(setOf(1, 2), setOf(3)), stronglyConnectedComponents(listOf(1)) {
            when (it) { 1 -> listOf(2); 2 -> listOf(1, 3); else -> emptyList() }
        }.map { it.toSet() }.toSet())
    }

    @Test
    fun longChain_shouldNotExhaustTheStack() {
        // given: 0 -> 1 -> ... -> 99999
        val n = 100_000
        val actual = stronglyConnectedComponents(listOf(0)) { if (it < n - 1) listOf(it + 1) else emptyList() }

        // then
        assertEquals(n, actual.size)
        assertEquals(n, actual.flatten().toSet().size)
    }

    @Test
    fun longCycle_shouldBeOneComponent() {
        val n = 100_000
        val actual = stronglyConnectedComponents(listOf(0)) { listOf((it + 1) % n) }
        assertEquals(1, actual.size)
        assertEquals(n, actual.single().size)
    }
}
