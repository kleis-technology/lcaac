package ch.kleis.lcaac.core.lang.evaluator.arena

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HeapTest {

    @Test
    fun addAndFind() {
        // given
        val heap = Heap<String>()

        // when
        val address = heap.add("hello")
        val actual = heap.find(address)

        // then
        assertEquals("hello", actual)
    }

    @Test
    fun modify() {
        // given
        val heap = Heap<String>()
        val address = heap.add("hello")

        // when
        heap.modify(address) { "world" }
        val actual = heap.find(address)

        // then
        assertEquals("world", actual)
    }

    @Test
    fun find_whenNotFound() {
        // given
        val heap = Heap<String>()

        // when
        val actual = heap.find(0)

        // then
        assertNull(actual)
    }

    @Test
    fun remove() {
        // given
        val heap = Heap<String>()
        val addressHello = heap.add("hello")
        val addressWorld = heap.add("world")

        // when
        heap.remove(addressHello)

        // then
        assertNull(heap.find(addressHello))
        assertEquals("world", heap.find(addressWorld))
    }

    @Test
    fun popAll() {
        // given
        val heap = Heap<String>()
        val addressHello = heap.add("hello")
        val addressWorld = heap.add("world")

        // when
        val actual = heap.popAll()

        // then
        assertNull(heap.find(addressHello))
        assertNull(heap.find(addressWorld))
        assertContentEquals(listOf("hello", "world"), actual)
    }
}
