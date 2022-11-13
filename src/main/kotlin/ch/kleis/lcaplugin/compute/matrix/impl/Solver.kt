package ch.kleis.lcaplugin.compute.matrix.impl

import java.util.*

interface Solver {
    fun solve(lhs: Matrix, rhs: Matrix): Matrix
    companion object {
        val INSTANCE: Solver = ServiceLoader.load(Solver::class.java).findFirst().orElseThrow()
    }
}
