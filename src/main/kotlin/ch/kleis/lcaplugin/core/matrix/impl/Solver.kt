package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.ejml.EJMLSolver

interface Solver {
    fun solve(lhs: Matrix, rhs: Matrix): Matrix?

    companion object {
        val INSTANCE: Solver = EJMLSolver()
    }
}
