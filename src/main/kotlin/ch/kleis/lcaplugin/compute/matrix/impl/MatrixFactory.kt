package ch.kleis.lcaplugin.compute.matrix.impl

import ch.kleis.lcaplugin.compute.matrix.impl.ojalgo.OjalgoMatrixFactory
import java.util.*

interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix
    companion object {
//        val INSTANCE: MatrixFactory = ServiceLoader.load(MatrixFactory::class.java).findFirst().orElseThrow()
        val INSTANCE: MatrixFactory = OjalgoMatrixFactory()
    }
}
