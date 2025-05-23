package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.HasUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IndexedCollectionTest {
    data class MyValue(
        val s: String
    ) : HasUID {
        override fun getUID(): String {
            return "my id is $s"
        }
    }

    @Test
    fun contains_whenExists() {
        // given
        val collection = IndexedCollection(
            listOf(MyValue("my id"), MyValue("my id2")),
        )
        val value = MyValue("my id")

        // when
        val actual = collection.contains(value)

        // then
        assertTrue(actual)
    }

    @Test
    fun contains_whenDoesNotExist() {
        // given
        val collection = IndexedCollection(
            listOf(MyValue("my id"), MyValue("my id2")),
        )
        val value = MyValue("my id does not exist")

        // when
        val actual = collection.contains(value)

        // then
        assertFalse(actual)
    }
}
