package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory

class MatrixFixture {
    companion object {
        fun make(rows: Int, cols: Int, data: Array<Double>): Matrix {
            val a = MatrixFactory.INSTANCE.zero(rows, cols)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if(data[cols * row + col] != 0.0) {
                        a.add(row, col, data[cols * row + col])
                    }
                }
            }
            return a
        }
    }
}
