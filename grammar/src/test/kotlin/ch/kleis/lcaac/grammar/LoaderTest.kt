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

    @Test
    fun load_dataExpression_priorityMulAdd() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = x * y + z
                    b = x + y * z
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EQuantityMul(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        val b = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityMul(EDataRef("y"), EDataRef("z")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityDivAdd() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = x / y + z
                    b = x + y / z
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EQuantityDiv(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        val b = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityDiv(EDataRef("y"), EDataRef("z")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityScaleMul() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = 3 x * y
                    b = x * 4 y
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityScale(BasicNumber(3.0), EQuantityMul(EDataRef("x"), EDataRef("y")))
        val b = EQuantityMul(EDataRef("x"), EQuantityScale(BasicNumber(4.0), EDataRef("y")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityScaleDiv() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = 3 x / y
                    b = x / 4 y
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityScale(BasicNumber(3.0), EQuantityDiv(EDataRef("x"), EDataRef("y")))
        val b = EQuantityDiv(EDataRef("x"), EQuantityScale(BasicNumber(4.0), EDataRef("y")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityPowAdd() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = x + y^2
                    b = x^2 + y
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityPow(EDataRef("y"), 2.0))
        val b = EQuantityAdd<BasicNumber>(EQuantityPow(EDataRef("x"), 2.0), EDataRef("y"))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_leftAssociativeDiv() {
        // given
        val file = lcaFile(
            """
                variables {
                    a = x / y / z
                }
            """.trimIndent()
        )
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityDiv<BasicNumber>(EQuantityDiv(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        assertEquals(a, actual.getData("a"))
    }
}
