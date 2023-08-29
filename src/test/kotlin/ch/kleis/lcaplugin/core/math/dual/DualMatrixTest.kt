package ch.kleis.lcaplugin.core.math.dual

import org.junit.Test
import kotlin.test.assertEquals


class DualMatrixTest {
    private val ops = DualOperations(2)
    private val dx = ops.basis(0)
    private val dy = ops.basis(1)
    private fun c(v: Double): DualNumber = ops.pure(v)

    private fun makeDualMatrix(rows: Int, cols: Int, data: Array<DualNumber>): DualMatrix {
        with(ops) {
            val a = zeros(rows, cols)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    a[row, col] = data[cols * row + col]
                }
            }
            return a
        }
    }


    @Test
    fun value() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(2.0) + dx, c(3.0),
                    c(4.0), c(5.0), c(6.0),
                )
            )

            // when
            val actual = m[0, 1]

            // then
            val expected = c(2.0) + dx
            assertEquals(expected, actual)
        }
    }

    @Test
    fun set() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(2.0), c(3.0),
                    c(4.0), c(5.0), c(6.0),
                )
            )

            // when
            m[0, 1] = pure(3.0) + dx

            // then
            val expected = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(3.0) + dx, c(3.0),
                    c(4.0), c(5.0), c(6.0),
                )
            )
            assertEquals(expected, m)
        }
    }


    @Test
    fun matMul() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 2,
                arrayOf(
                    c(1.0) + dx, c(2.0),
                    c(3.0), c(4.0) + dy,
                )
            )
            val x = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(2.0) + dx, c(3.0) + dy,
                    c(1.0), c(2.0) + dy, c(3.0) + dx,
                )
            )

            // when
            val actual = m.matMul(x)

            // then
            /*
                m = [
                    1 + dx,  2
                    3,       4 + dy
                ]
                x = [
                    1,   2+dx,    3+dy
                    1,   2+dy,    3+dx
                ]
                m @ x = [
                    (1+dx) + 2,  (1+dx)(2+dx) + 2(2+dy), (1+dx)(3+dy) + 2(3+dx)
                    3 + (4+dy), 3(2+dx) + (4+dy)(2+dy), 3(3+dy) + (4+dy)(3+dx)
                ] = [
                    3 + dx, 6 + 3dx + 2dy, 9 + 5dx + 1dy
                    7 + dy, 14 + 3dx + 6dy, 21 + 4dx + 6dy
                ]
             */
            val expected = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(3.0) + dx, c(6.0) + c(3.0) * dx + c(2.0) * dy, c(9.0) + c(5.0) * dx + c(1.0) * dy,
                    c(7.0) + dy, c(14.0) + c(3.0) * dx + c(6.0) * dy, c(21.0) + c(4.0) * dx + c(6.0) * dy,
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun matDiv() {
        with(ops) {
            // given
            val lhs = makeDualMatrix(
                2, 2,
                arrayOf(
                    c(1.0) + dx, c(0.0),
                    c(0.0), c(1.0) - dy,
                )
            )
            val rhs = makeDualMatrix(
                2, 1,
                arrayOf(
                    c(1.0),
                    c(1.0),
                )
            )

            // when
            val actual = rhs.matDiv(lhs)!!

            // then
            val expected = makeDualMatrix(
                2, 1,
                arrayOf(
                    c(1.0) - dx,
                    c(1.0) + dy,
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun negate() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 2,
                arrayOf(
                    c(1.0) + dx + dy, c(2.0),
                    c(3.0), c(4.0) - dx - dy,
                )
            )

            // when
            val actual = m.negate()

            // then
            val expected = makeDualMatrix(
                2, 2,
                arrayOf(
                    -c(1.0) - dx - dy, -c(2.0),
                    -c(3.0), -c(4.0) + dx + dy,
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun transpose() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 2,
                arrayOf(
                    c(1.0) + dx + dy, c(2.0),
                    c(3.0), c(4.0) - dx - dy,
                )
            )

            // when
            val actual = m.transpose()

            // then
            val expected = makeDualMatrix(
                2, 2,
                arrayOf(
                    c(1.0) + dx + dy, c(3.0),
                    c(2.0), c(4.0) - dx - dy,
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun rowDim() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(2.0), c(3.0),
                    c(4.0), c(5.0), c(6.0),
                )
            )

            // when
            val actual = m.rowDim()

            // then
            assertEquals(2, actual)
        }
    }

    @Test
    fun colDim() {
        with(ops) {
            // given
            val m = makeDualMatrix(
                2, 3,
                arrayOf(
                    c(1.0), c(2.0), c(3.0),
                    c(4.0), c(5.0), c(6.0),
                )
            )

            // when
            val actual = m.colDim()

            // then
            assertEquals(3, actual)
        }
    }
}
