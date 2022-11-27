package ch.kleis.lcaplugin.lib.matrix.impl

import ch.kleis.lcaplugin.lib.matrix.impl.ojalgo.OjalgoMatrixFactory

interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix
    companion object {
//        val INSTANCE: MatrixFactory = ServiceLoader.load(MatrixFactory::class.java).findFirst().orElseThrow()
        val INSTANCE: MatrixFactory = OjalgoMatrixFactory()
    }
}
