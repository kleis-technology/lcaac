package ch.kleis.lcaplugin.core.matrix.impl.ojalgo

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.Solver
import ch.kleis.lcaplugin.core.matrix.impl.ojalgo.OjalgoMatrix
import ch.kleis.lcaplugin.core.matrix.impl.ojalgo.OjalgoMatrixFactory
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
