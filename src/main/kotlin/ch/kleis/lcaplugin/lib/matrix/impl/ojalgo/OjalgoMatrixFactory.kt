package ch.kleis.lcaplugin.lib.matrix.impl.ojalgo

import ch.kleis.lcaplugin.lib.matrix.impl.Matrix
import ch.kleis.lcaplugin.lib.matrix.impl.MatrixFactory

import org.ojalgo.matrix.store.Primitive64Store.FACTORY;

class OjalgoMatrixFactory : MatrixFactory {
    override fun zero(nRows: Int, nCols: Int): Matrix {
        return OjalgoMatrix(FACTORY.make(nRows.toLong(), nCols.toLong()))
    }
}
