package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicOperations

class BasicMatrixFixture {
    companion object {
        private val ops = BasicOperations()
        fun make(rows: Int, cols: Int, data: Array<Double>): BasicMatrix {
            with(ops) {
                val a = zeros(rows, cols)
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if(data[cols * row + col] != 0.0) {
                            a.add(row, col, pure(data[cols * row + col]))
                        }
                    }
                }
                return a
            }
        }
    }
}
