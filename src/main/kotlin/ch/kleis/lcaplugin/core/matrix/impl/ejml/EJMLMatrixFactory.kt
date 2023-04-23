package ch.kleis.lcaplugin.core.matrix.impl.ejml

import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory
import org.ejml.data.MatrixType.DSCC
import org.ejml.simple.SimpleMatrix

class EJMLMatrixFactory : MatrixFactory {
    override fun zero(nRows: Int, nCols: Int): Matrix =
        EJMLMatrix(SimpleMatrix(nRows, nCols, DSCC))

}