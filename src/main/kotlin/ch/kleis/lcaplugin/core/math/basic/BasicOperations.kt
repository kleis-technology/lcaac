package ch.kleis.lcaplugin.core.math.basic

import ch.kleis.lcaplugin.core.math.Operations
import com.intellij.openapi.diagnostic.Logger
import org.ejml.data.DMatrixSparseCSC
import org.ejml.data.MatrixType
import org.ejml.simple.SimpleMatrix
import org.ejml.sparse.csc.CommonOps_DSCC
import kotlin.math.pow

object BasicOperations : Operations<BasicNumber, BasicMatrix> {
    private val LOG = Logger.getInstance(BasicOperations::class.java)

    override fun BasicNumber.plus(other: BasicNumber): BasicNumber {
        return BasicNumber(value + other.value)
    }

    override fun BasicNumber.minus(other: BasicNumber): BasicNumber {
        return BasicNumber(value - other.value)
    }

    override fun BasicNumber.times(other: BasicNumber): BasicNumber {
        return BasicNumber(value * other.value)
    }

    override fun BasicNumber.div(other: BasicNumber): BasicNumber {
        return BasicNumber(value / other.value)
    }

    override fun BasicMatrix.matMul(other: BasicMatrix): BasicMatrix {
        val result = DMatrixSparseCSC(this.rowDim(), other.colDim())
        CommonOps_DSCC.mult(inner.dscc, other.inner.dscc, result)
        return BasicMatrix(
            SimpleMatrix.wrap(result)
        )
    }

    override fun BasicMatrix.matDiv(other: BasicMatrix): BasicMatrix? {
        LOG.info("Start solving lhs(${other.rowDim()}, ${other.rowDim()}) * x = rhs(${this.rowDim()}, ${this.colDim()})")
        val lhs = other.inner.dscc
        val rhs = this.inner.dscc
        val result = DMatrixSparseCSC(lhs.numCols, rhs.numCols)
        return if (CommonOps_DSCC.solve(lhs, rhs, result)) {
            LOG.info("End solving with result(${result.numRows}, ${result.numCols})")
            BasicMatrix(
                SimpleMatrix.wrap(result)
            )
        } else {
            null
        }
    }

    override fun BasicNumber.pow(other: Double): BasicNumber {
        return BasicNumber(value.pow(other))
    }

    override fun BasicNumber.toDouble(): Double {
        return this.value
    }

    override fun BasicNumber.unaryMinus(): BasicNumber {
        return BasicNumber(-this.value)
    }

    override fun pure(value: Double): BasicNumber {
        return BasicNumber(value)
    }

    override fun zeros(rowDim: Int, colDim: Int): BasicMatrix {
        return BasicMatrix(SimpleMatrix(rowDim, colDim, MatrixType.DSCC))
    }

    override fun BasicMatrix.colDim(): Int {
        return this.inner.numCols
    }

    override fun BasicMatrix.rowDim(): Int {
        return this.inner.numRows
    }

    override fun BasicMatrix.set(row: Int, col: Int, value: BasicNumber) {
        this.inner[row, col] = value.value
    }

    override fun BasicMatrix.get(row: Int, col: Int): BasicNumber {
        return pure(this.inner[row, col])
    }

    override fun BasicMatrix.transpose(): BasicMatrix {
        return BasicMatrix(inner.transpose())
    }

    override fun BasicMatrix.negate(): BasicMatrix {
        return BasicMatrix(inner.negative())
    }
}
