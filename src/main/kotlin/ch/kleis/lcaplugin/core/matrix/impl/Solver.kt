package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.ojalgo.OjalgoSolver

interface Solver {
    fun solve(lhs: Matrix, rhs: Matrix): Matrix?
    companion object {
        val INSTANCE: Solver = OjalgoSolver()
    }
}
