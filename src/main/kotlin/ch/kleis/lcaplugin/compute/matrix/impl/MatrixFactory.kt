package ch.kleis.lcaplugin.compute.matrix.impl

import java.util.*

sealed interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix
    companion object {
        val INSTANCE: MatrixFactory = ServiceLoader.load(MatrixFactory::class.java).findFirst().orElseThrow()
    }
}
