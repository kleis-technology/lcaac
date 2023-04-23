package ch.kleis.lcaplugin.core.matrix.impl.ejml

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.Solver
import com.intellij.openapi.diagnostic.Logger
import org.ejml.data.DMatrixSparseCSC
import org.ejml.simple.SimpleMatrix.wrap
import org.ejml.sparse.csc.CommonOps_DSCC

class EJMLSolver : Solver {

    companion object {
        private val LOG = Logger.getInstance(EJMLSolver::class.java)
    }

    override fun solve(lhs: Matrix, rhs: Matrix): Matrix? {
        LOG.info("Start solving lhs(${lhs.colDim()}, ${lhs.rowDim()}) * x = rhs(${rhs.colDim()}, ${rhs.rowDim()})")
        val a = (lhs as EJMLMatrix).matrix
        val b = (rhs as EJMLMatrix).matrix
        if (a.numRows == 0 || b.numCols == 0) {
            val result = EJMLMatrixFactory().zero(a.numRows, b.numCols)
            LOG.info("End solving matrix with 0")
            return result
        }
        val x = DMatrixSparseCSC(a.numCols, b.numCols)
        return if (CommonOps_DSCC.solve(a.dscc, b.dscc, x)) {
            LOG.info("End solving matrix with result ${x.numRows} rows and ${x.numCols} cols")
            EJMLMatrix(wrap(x))
        } else {
            null
        }
    }

}