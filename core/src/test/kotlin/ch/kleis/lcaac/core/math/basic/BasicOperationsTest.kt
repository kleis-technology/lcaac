package ch.kleis.lcaac.core.math.basic

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class BasicOperationsTest {
    private val precision = 1e-6
    private val ops = BasicOperations

    @Test
    fun test_pure() {
        with(ops) {
            // given
            val v = 1.0

            // when
            val actual = pure(v)

            // then
            assertEquals(BasicNumber(1.0), actual)
        }
    }

    @Test
    fun test_plus() {
        with(ops) {
            // given
            val l = BasicNumber(1.0)
            val r = BasicNumber(2.0)

            // when
            val actual = l + r

            // then
            assertEquals(BasicNumber(1.0 + 2.0), actual)
        }
    }

    @Test
    fun test_minus() {
        with(ops) {
            // given
            val l = BasicNumber(1.0)
            val r = BasicNumber(2.0)

            // when
            val actual = l - r

            // then
            assertEquals(BasicNumber(1.0 - 2.0), actual)
        }
    }

    @Test
    fun test_times() {
        with(ops) {
            // given
            val l = BasicNumber(1.0)
            val r = BasicNumber(2.0)

            // when
            val actual = l * r

            // then
            assertEquals(BasicNumber(1.0 * 2.0), actual)
        }
    }

    @Test
    fun test_div() {
        with(ops) {
            // given
            val l = BasicNumber(1.0)
            val r = BasicNumber(2.0)

            // when
            val actual = l / r

            // then
            assertEquals(BasicNumber(1.0 / 2.0), actual)
        }
    }

    @Test
    fun test_matDiv() {
        with(ops) {
            // given
            val lhs = MatrixFixture.basic(
                2, 2, arrayOf(
                    2.0, 0.0,
                    0.0, 4.0,
                )
            )
            val rhs = MatrixFixture.basic(
                2, 3, arrayOf(
                    1.0, 0.0, 0.0,
                    0.0, 2.0, 0.0,
                )
            )

            // when
            val actual = rhs.matDiv(lhs)!!

            // then
            assertBasicMatrixEqual(
                actual, arrayOf(
                    0.5, 0.0, 0.0,
                    0.0, 0.5, 0.0,
                )
            )
        }
    }

    @Test
    fun test_matDiv_whenZeroCols() {
        with(ops) {
            // given
            val lhs = zeros(3, 0)
            val rhs = MatrixFixture.basic(
                3, 1, arrayOf(
                    1.0,
                    2.0,
                    3.0
                )
            )

            // when
            val actual = rhs.matDiv(lhs)!!

            // then
            assertEquals(actual.rowDim(), 0)
            assertEquals(actual.colDim(), 1)
        }
    }

    @Test
    fun test_matDiv_whenNonInvertible() {
        with(ops) {
            // given
            val lhs = MatrixFixture.basic(
                3, 3, arrayOf(
                    1.0, -2.0, 0.0,
                    0.0, 1.0, 0.0,
                    0.0, 0.0, 0.0,
                )
            )
            val rhs = MatrixFixture.basic(
                3, 2, arrayOf(
                    1.0, 4.0,
                    1.0, 2.0,
                    1.0, 1.0,
                )
            )

            // when
            val actual = rhs.matDiv(lhs)

            // then
            assertNull(actual)
        }
    }

    @Test
    fun test_matMul() {
        with(ops) {
            // given
            val a = MatrixFixture.basic(
                3, 3, arrayOf(
                    1.0, 1.0, 0.0,
                    0.0, 1.0, 0.0,
                    0.0, 0.0, 1.0,
                )
            )
            val b = MatrixFixture.basic(
                3, 2, arrayOf(
                    1.0, 0.0,
                    0.0, 1.0,
                    1.0, 1.0,
                )
            )

            // when
            val actual = a.matMul(b)

            // then
            assertBasicMatrixEqual(
                actual, arrayOf(
                    1.0, 1.0,
                    0.0, 1.0,
                    1.0, 1.0,
                )
            )
        }
    }

    @Test
    fun test_negate() {
        with(ops) {
            // given
            val a = MatrixFixture.basic(
                2, 2, arrayOf(
                    1.0, 2.0,
                    3.0, 4.0,
                )
            )

            // when
            val actual = a.negate()

            // then
            assertBasicMatrixEqual(
                actual, arrayOf(
                    -1.0, -2.0,
                    -3.0, -4.0,
                )
            )
        }
    }

    @Test
    fun test_setAndGet() {
        with(ops) {
            // given
            val a = MatrixFixture.basic(
                2, 2, arrayOf(
                    1.0, 2.0,
                    3.0, 4.0,
                )
            )

            // when
            a[0, 0] = pure(3.0)
            val actual = a[0,0]

            // then
            assertEquals(3.0, actual.value)
        }
    }

    @Test
    fun test_transpose() {
        with(ops) {
            // given
            val a = MatrixFixture.basic(
                2, 2, arrayOf(
                    1.0, 2.0,
                    3.0, 4.0,
                )
            )

            // when
            val actual = a.transpose()

            // then
            assertBasicMatrixEqual(
                actual, arrayOf(
                    1.0, 3.0,
                    2.0, 4.0,
                )
            )
        }
    }


    private fun assertBasicMatrixEqual(actual: BasicMatrix, expected: Array<Double>) {
        with(ops) {
            assertEquals(expected.size, actual.rowDim() * actual.colDim())
            for (row in 0 until actual.rowDim()) {
                for (col in 0 until actual.colDim()) {
                    assertEquals(
                        expected[row * actual.colDim() + col],
                        actual[row, col].value,
                        precision,
                        "(row ${row}, col ${col}):",
                    )
                }
            }
        }
    }
}
