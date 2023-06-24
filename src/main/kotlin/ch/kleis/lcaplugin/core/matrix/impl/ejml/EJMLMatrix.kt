package ch.kleis.lcaplugin.core.matrix.impl.ejml

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import org.ejml.simple.SimpleMatrix

class EJMLMatrix(internal val matrix: SimpleMatrix) : Matrix {
    override fun value(row: Int, col: Int): Double =
        matrix[row, col]

    override fun set(row: Int, col: Int, value: Double) =
        matrix.set(row, col, value)

    override fun negate(): Matrix = EJMLMatrix(matrix.negative())
    override fun transpose(): Matrix = EJMLMatrix(matrix.transpose())

    override fun rowDim(): Int = matrix.numRows

    override fun colDim(): Int = matrix.numCols
}
