package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals

class QuantityValueOperationsTest {
    private val ops = BasicOperations
    private val quantityOps = QuantityValueOperations(ops)

    @Test
    fun plus() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.g())

        // when
        with(quantityOps) {
            val actual = a + b

            // then
            val expected = QuantityValue(ops.pure(3.0), UnitValueFixture.kg())
            assertEquals(expected, actual)
        }
    }

    @Test
    fun minus() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.g())

        // when
        with(quantityOps) {
            val actual = a - b

            // then
            val expected = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
            assertEquals(expected, actual)
        }
    }

    @Test
    fun plus_whenIncompatibleDims() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.l())

        // when
        val e = assertThrows(EvaluatorException::class.java) { with(quantityOps) { a + b } }
        assertEquals("incompatible dimensions: mass vs length³ in left=kg and right=l", e.message)
    }

    @Test
    fun minus_whenIncompatibleDims() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.l())

        // when
        val e = assertThrows(EvaluatorException::class.java) { with(quantityOps) { a - b } }
        assertEquals("incompatible dimensions: mass vs length³ in left=kg and right=l", e.message)
    }

    @Test
    fun times() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.l())

        // when
        with(quantityOps) {
            val actual = a * b

            // then
            val expected = QuantityValue(ops.pure(2000.0), UnitValueFixture.kg<BasicNumber>() * UnitValueFixture.l())
            assertEquals(expected, actual)
        }
    }

    @Test
    fun div() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val b = QuantityValue(ops.pure(1000.0), UnitValueFixture.l())

        // when
        with(quantityOps) {
            val actual = a / b

            // then
            val expected = QuantityValue(ops.pure(2E-3), UnitValueFixture.kg<BasicNumber>() / UnitValueFixture.l())
            assertEquals(expected, actual)
        }
    }

    @Test
    fun unaryMinus() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())

        // when
        with(quantityOps) {
            val actual = -a

            // then
            val expected = QuantityValue(ops.pure(-2.0), UnitValueFixture.kg())
            assertEquals(expected, actual)
        }
    }

    @Test
    fun pow() {
        // given
        val a = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val n = 2.0

        // when
        with(quantityOps) {
            val actual = a.pow(n)

            // then
            val expected = QuantityValue(ops.pure(4.0), UnitValueFixture.kg<BasicNumber>().pow(n))
            assertEquals(expected, actual)
        }
    }

    @Test
    fun absoluteScaleValue() {
        // given
        val foo = UnitValue<BasicNumber>(UnitSymbol.of("foo"), 123.0, Dimension.of("foo"))
        val a = QuantityValue(ops.pure(2.0), foo)

        // when
        with(quantityOps) {
            val actual = a.absoluteScaleValue()

            // then
            val expected = BasicNumber(2.0 * 123.0)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun toDouble() {
        // given
        val foo = UnitValue<BasicNumber>(UnitSymbol.of("foo"), 123.0, Dimension.of("foo"))
        val a = QuantityValue(ops.pure(2.0), foo)

        // when
        with(quantityOps) {
            val actual = a.toDouble()

            // then
            val expected = 2.0 * 123.0
            assertEquals(expected, actual)
        }
    }

    @Test
    fun pure() {
        // when
        with(quantityOps) {
            val actual = pure(1.0)

            // then
            val expected = QuantityValue(ops.pure(1.0), UnitValue.none())
            assertEquals(expected, actual)
        }
    }
}
