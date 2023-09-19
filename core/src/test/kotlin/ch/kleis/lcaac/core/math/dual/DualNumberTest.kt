package ch.kleis.lcaac.core.math.dual

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals


class DualNumberTest {
    private val ops = DualOperations(2)
    private val dx = ops.basis(0)
    private val dy = ops.basis(1)

    @Test
    fun basis() {
        with(DualOperations(3)) {
            // when
            val actual = basis(1)

            // then
            val expected = DualNumber(
                0.0,
                mk.ndarray(
                    listOf(
                        0.0, 1.0, 0.0,
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun pure() {
        with(ops) {
            // when
            val actual = pure(1.0)

            // then
            val expected = DualNumber(
                1.0,
                mk.zeros(2),
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun pow() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val e = 3.0

            // when
            val actual = x.pow(e)

            // then
            val expected = DualNumber(
                8.0,
                mk.ndarray(
                    listOf(
                        3.0 * 2.0.pow(2), 0.0,
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun add() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0) + dy

            // when
            val actual = x + y

            // then
            val expected = DualNumber(
                5.0, // x + y
                mk.ndarray(
                    listOf(
                        1.0, 1.0, // dx + dy
                    )
                )
            )
            assertEquals(expected, actual)

        }
    }

    @Test
    fun add_constant_left() {
        with(ops) {
            // given
            val x = pure(2.0)
            val y = pure(3.0) + dy

            // when
            val actual = x + y

            // then
            val expected = DualNumber(
                5.0, // x + y
                mk.ndarray(
                    listOf(
                        0.0, 1.0, // dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun add_constant_right() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0)

            // when
            val actual = x + y

            // then
            val expected = DualNumber(
                5.0, // x + y
                mk.ndarray(
                    listOf(
                        1.0, 0.0, // dx
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun sub() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0) + dy

            // when
            val actual = x - y

            // then
            val expected = DualNumber(
                -1.0, // x - y
                mk.ndarray(
                    listOf(
                        1.0, -1.0, // dx - dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun sub_constant_left() {
        with(ops) {
            // given
            val x = pure(2.0)
            val y = pure(3.0) + dy

            // when
            val actual = x - y

            // then
            val expected = DualNumber(
                -1.0, // x - y
                mk.ndarray(
                    listOf(
                        0.0, -1.0, // - dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun sub_constant_right() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0)

            // when
            val actual = x - y

            // then
            val expected = DualNumber(
                -1.0, // x - y
                mk.ndarray(
                    listOf(
                        1.0, 0.0, // dx
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun times() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0) + dy

            // when
            val actual = x * y

            // then
            val expected = DualNumber(
                6.0, // x * y
                mk.ndarray(
                    listOf(
                        3.0, 2.0, // y.dx + x.dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun times_constant_left() {
        with(ops) {
            // given
            val x = pure(2.0)
            val y = pure(3.0) + dy

            // when
            val actual = x * y

            // then
            val expected = DualNumber(
                6.0, // x * y
                mk.ndarray(
                    listOf(
                        0.0, 2.0, // x.dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun times_constant_right() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(3.0)

            // when
            val actual = x * y

            // then
            val expected = DualNumber(
                6.0, // x * y
                mk.ndarray(
                    listOf(
                        3.0, 0.0, // y.dx
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun div() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(4.0) + dy

            // when
            val actual = x / y

            // then
            val expected = DualNumber(
                0.5, // x / y
                mk.ndarray(
                    listOf(
                        0.25, -0.125, // (1/y).dx - (x/y2).dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun div_constant_left() {
        with(ops) {
            // given
            val x = pure(2.0)
            val y = pure(4.0) + dy

            // when
            val actual = x / y

            // then
            val expected = DualNumber(
                0.5, // x / y
                mk.ndarray(
                    listOf(
                        0.0, -0.125, // (1/y).dx - (x/y2).dy
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun div_constant_right() {
        with(ops) {
            // given
            val x = pure(2.0) + dx
            val y = pure(4.0)

            // when
            val actual = x / y

            // then
            val expected = DualNumber(
                0.5, // x / y
                mk.ndarray(
                    listOf(
                        0.25, 0.0, // (1/y).dx
                    )
                )
            )
            assertEquals(expected, actual)
        }
    }
}
