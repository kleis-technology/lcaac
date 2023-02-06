package ch.kleis.lcaplugin.compute.matrix.impl.ojalgo

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.Solver
import org.ojalgo.RecoverableCondition
import org.ojalgo.matrix.task.SolverTask

class OjalgoSolver : Solver {
    override fun solve(lhs: Matrix, rhs: Matrix): Matrix? {
        val a = lhs as OjalgoMatrix
        val b = rhs as OjalgoMatrix
        if (a.rowDim() == 0 || b.colDim() == 0) {
            return OjalgoMatrixFactory().zero(a.rowDim(), b.colDim())
        }
        return try {
            OjalgoMatrix(SolverTask.PRIMITIVE.solve(a.store, b.store))
        } catch (e: RecoverableCondition) {
            null
        }
    }
}
