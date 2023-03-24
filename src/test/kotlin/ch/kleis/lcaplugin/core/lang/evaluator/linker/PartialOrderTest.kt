package ch.kleis.lcaplugin.core.lang.evaluator.linker

import com.intellij.openapi.ui.naturalSorted
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PartialOrderTest {
    inner class SetPartialOrder : PartialOrder<Set<String>> {
        override fun leq(a: Set<String>, b: Set<String>): Boolean {
            return b.containsAll(a)
        }
    }

    @Test
    fun minimal() {
        // given
        val poset = SetPartialOrder()
        val a = setOf("a")
        val ab = setOf("a", "b")
        val abcd = setOf("a", "b", "c", "d")
        val cd = setOf("c", "d")
        val d = setOf("d")
        val elements = listOf(a, ab, abcd, cd, d)

        // when
        val actual = poset.minimal(elements).naturalSorted()

        // then
        val expected = listOf(
            setOf("a"), setOf("d")
        ).naturalSorted()
        assertEquals(expected, actual)
    }
}
