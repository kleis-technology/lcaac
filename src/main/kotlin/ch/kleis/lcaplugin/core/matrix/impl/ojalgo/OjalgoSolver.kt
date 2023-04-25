package ch.kleis.lcaplugin.core.matrix.impl.ojalgo

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.Solver
import com.intellij.openapi.diagnostic.Logger
import org.ojalgo.RecoverableCondition
import org.ojalgo.matrix.task.SolverTask

class OjalgoSolver : Solver {
    companion object {
        private val LOG = Logger.getInstance(OjalgoSolver::class.java)
    }

    override fun solve(lhs: Matrix, rhs: Matrix): Matrix? {
        LOG.info("Start solving matrix lhs(${lhs.colDim()}, ${lhs.rowDim()}) and rhs(${rhs.colDim()}, ${rhs.rowDim()})")
        val a = lhs as OjalgoMatrix
        val b = rhs as OjalgoMatrix
        if (a.rowDim() == 0 || b.colDim() == 0) {
            val result = OjalgoMatrixFactory().zero(a.rowDim(), b.colDim())
            LOG.info("End solving matrix with 0")
            return result
        }
        return try {
            val result = OjalgoMatrix(SolverTask.PRIMITIVE.solve(a.store, b.store))
            LOG.info("End solving matrix with result ${result.rowDim()} rows and ${result.colDim()} cols")
            return result
        } catch (e: RecoverableCondition) {
            LOG.info("End solving matrix with error ${e.message}")
            null
        }
    }
}
