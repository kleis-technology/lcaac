package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.expression.EDataRef
import ch.kleis.lcaplugin.core.lang.expression.name
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EDataRef<BasicNumber>("a")

        val key2 = "abc.y"
        val b = EDataRef<BasicNumber>("b")
        val register = Register.empty<EDataRef<BasicNumber>>().plus(listOf(key to a, key2 to b))
        val index = Index(
            register,
            object : IndexKeySerializer<String> {
                override fun serialize(key: String): String = key
            },
            EDataRef.name(),
        )

        // when
        val actual = index.firstOrNull("a")

        // then
        assertEquals(a, actual)
    }

}
