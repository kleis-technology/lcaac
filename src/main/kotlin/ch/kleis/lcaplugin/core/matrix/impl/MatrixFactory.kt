package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.ojalgo.OjalgoMatrixFactory

interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix
    companion object {
        val INSTANCE: MatrixFactory = OjalgoMatrixFactory()
    }
}
