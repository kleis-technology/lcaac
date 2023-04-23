package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.ejml.EJMLMatrixFactory

interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix

    companion object {
        val INSTANCE: MatrixFactory =
            EJMLMatrixFactory()
    }
}
