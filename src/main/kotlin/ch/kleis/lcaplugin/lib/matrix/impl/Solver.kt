package ch.kleis.lcaplugin.lib.matrix.impl

import ch.kleis.lcaplugin.lib.matrix.impl.ojalgo.OjalgoSolver

interface Solver {
    fun solve(lhs: Matrix, rhs: Matrix): Matrix
    companion object {
//        val INSTANCE: Solver = ServiceLoader.load(Solver::class.java).findFirst().orElseThrow()
        val INSTANCE: Solver = OjalgoSolver()
    }
}
