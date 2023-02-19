package ch.kleis.lcaplugin.core.matrix.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolverTest {
    private val precision = 1e-6

    @Test
    fun solve_whenNormal() {
        // given
        val a = make(
            2, 2, arrayOf(
                2.0, 0.0,
                0.0, 4.0,
            )
        )
        val b = make(
            2, 3, arrayOf(
                1.0, 0.0, 0.0,
                0.0, 2.0, 0.0,
            )
        )

        // when
        val c = Solver.INSTANCE.solve(a, b)!!

        // then
        assertMatrixEqual(
            c, arrayOf(
                0.5, 0.0, 0.0,
                0.0, 0.5, 0.0,
            )
        )
    }

    @Test
    fun solve_whenZeroCols() {
        // given
        val a = MatrixFactory.INSTANCE.zero(0, 3)
        val b = make(
            3, 1, arrayOf(
                1.0,
                2.0,
                3.0
            )
        )

        // when
        val c = Solver.INSTANCE.solve(a, b)!!

        // then
        assertEquals(c.rowDim(), a.rowDim())
        assertEquals(c.colDim(), b.colDim())
    }

    @Test
    fun solve_whenNonInvertible() {
        // given
        val a = make(
            3, 3, arrayOf(
                1.0, -2.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 0.0,
            )
        )
        val b = make(
            2, 2, arrayOf(
                1.0, 4.0,
                1.0, 2.0,
            )
        )

        // when
        val c = Solver.INSTANCE.solve(a, b)

        // then
        assertNull(c)
    }

    private fun assertMatrixEqual(actual: Matrix, expected: Array<Double>) {
        assertEquals(expected.size, actual.rowDim() * actual.colDim())
        for (row in 0 until actual.rowDim()) {
            for (col in 0 until actual.colDim()) {
                assertEquals(
                    "(row ${row}, col ${col}):",
                    expected[row * actual.colDim() + col],
                    actual.value(row, col),
                    precision
                )
            }
        }
    }

    private fun make(rows: Int, cols: Int, data: Array<Double>): Matrix {
        val a = MatrixFactory.INSTANCE.zero(rows, cols)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                a.add(row, col, data[cols * row + col])
            }
        }
        return a
    }
}
