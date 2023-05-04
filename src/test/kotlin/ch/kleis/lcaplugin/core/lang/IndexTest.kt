package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.expression.EQuantityRef
import ch.kleis.lcaplugin.core.lang.expression.name
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")

        val key2 = "abc.y"
        val b = EQuantityRef("b")
        val register = Register.empty<EQuantityRef>().plus(listOf(key to a, key2 to b))
        val index = Index(
            register,
            object : IndexKeyDescriptor<String> {
                override fun serialize(key: String): String = key
            },
            EQuantityRef.name,
        )

        // when
        val actual = index["a"]

        // then
        assertEquals(a, actual)
    }

}
