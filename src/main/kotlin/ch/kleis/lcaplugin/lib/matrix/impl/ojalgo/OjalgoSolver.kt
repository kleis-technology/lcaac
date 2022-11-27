package ch.kleis.lcaplugin.lib.matrix.impl.ojalgo

import ch.kleis.lcaplugin.lib.matrix.impl.Matrix
import ch.kleis.lcaplugin.lib.matrix.impl.Solver

import org.ojalgo.matrix.task.SolverTask;

class OjalgoSolver : Solver {
    override fun solve(lhs: Matrix, rhs: Matrix): Matrix {
        val a = lhs as OjalgoMatrix
        val b = rhs as OjalgoMatrix
        return OjalgoMatrix(SolverTask.PRIMITIVE.solve(a.store, b.store))
    }
}
