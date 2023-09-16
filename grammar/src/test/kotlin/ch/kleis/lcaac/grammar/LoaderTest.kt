package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.LcaLangFixture.Companion.lcaFile
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LoaderTest {
    @Test
    fun load_process() {
        // given
        val file = lcaFile(
            """
                process p {
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate<BasicNumber>(
            body = EProcess(
                "p",
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_dataExpression_basic() {
        // given
        val file = lcaFile(
            """
                variables {
                    sum = x + y
                    mul = x * y
                    div = x / y
                    scale = 2 x
                    pow = x^2.0
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val sum = EQuantityAdd<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val mul = EQuantityMul<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val div = EQuantityDiv<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val scale = EQuantityScale<BasicNumber>(BasicNumber(2.0), EDataRef("x"))
        val pow = EQuantityPow<BasicNumber>(EDataRef("x"), 2.0)
        assertEquals(sum, actual.getData("sum"))
        assertEquals(mul, actual.getData("mul"))
        assertEquals(div, actual.getData("div"))
        assertEquals(scale, actual.getData("scale"))
        assertEquals(pow, actual.getData("pow"))
    }
}
