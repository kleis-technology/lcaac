package ch.kleis.lcaac.core.lang

import arrow.optics.Fold
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IndexTest {
    private val dataRefKeyOptics = object : Fold<DataExpression<BasicNumber>, String> {
        override fun <R> foldMap(M: Monoid<R>, source: DataExpression<BasicNumber>, map: (focus: String) -> R): R {
            return when(source) {
                is EDataRef -> map(source.name)
                else -> fail("should not happen")
            }
        }
    }

    @Test
    fun set_and_get() {
        // given
        val key = DataKey("abc.x")
        val a = EDataRef<BasicNumber>("a")

        val key2 = DataKey("abc.y")
        val b = EDataRef<BasicNumber>("b")
        val register = DataRegister(mapOf(key to a, key2 to b))
        val index = Index(
            register,
            dataRefKeyOptics,
        )

        // when
        val actual = index.firstOrNull("a")

        // then
        assertEquals(a, actual)
    }

}
