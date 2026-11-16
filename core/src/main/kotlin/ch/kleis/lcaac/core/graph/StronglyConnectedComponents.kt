package ch.kleis.lcaac.core.graph

/**
 * The strongly connected components of a directed graph (Tarjan's algorithm): the largest groups
 * of nodes that can all reach each other. Every node reachable from [nodes] is in exactly one component.
 *
 * The depth-first search keeps its own stack of frames instead of recursing, so that a long path
 * (e.g. a supply chain of thousands of processes) does not exhaust the call stack.
 */
fun <N> stronglyConnectedComponents(nodes: Collection<N>, edges: (N) -> Collection<N>): List<List<N>> {
    val index = HashMap<N, Int>()
    val lowLink = HashMap<N, Int>()
    val stack = ArrayDeque<N>()
    val onStack = HashSet<N>()
    val components = ArrayList<List<N>>()

    // A node being visited, and the edges it has not followed yet
    class Frame(val node: N, val next: Iterator<N>)
    val callStack = ArrayDeque<Frame>()

    fun enter(node: N) {
        index[node] = index.size
        lowLink[node] = index.getValue(node)
        stack.addLast(node)
        onStack.add(node)
        callStack.addLast(Frame(node, edges(node).iterator()))
    }

    nodes.forEach { root ->
        if (root in index) return@forEach
        enter(root)
        while (callStack.isNotEmpty()) {
            val frame = callStack.last()
            if (frame.next.hasNext()) {
                val next = frame.next.next()
                if (next !in index) {
                    enter(next)
                } else if (next in onStack) {
                    lowLink[frame.node] = minOf(lowLink.getValue(frame.node), index.getValue(next))
                }
                continue
            }

            // All the edges of the node are followed: close its component if it is the root of one
            callStack.removeLast()
            if (lowLink[frame.node] == index[frame.node]) {
                val component = ArrayList<N>()
                do {
                    val member = stack.removeLast()
                    onStack.remove(member)
                    component.add(member)
                } while (member != frame.node)
                components.add(component)
            }
            callStack.lastOrNull()?.let { parent ->
                lowLink[parent.node] = minOf(lowLink.getValue(parent.node), lowLink.getValue(frame.node))
            }
        }
    }
    return components
}
