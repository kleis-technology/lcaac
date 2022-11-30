package ch.kleis.lcaplugin.compute.matrix.impl.ojalgo

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import org.ojalgo.matrix.store.MatrixStore
import org.ojalgo.matrix.store.Primitive64Store

class OjalgoMatrix(val store: MatrixStore<Double>) : Matrix {

    override fun value(row: Int, col: Int): Double {
        return store.doubleValue(row.toLong(), col.toLong())
    }

    override fun add(row: Int, col: Int, value: Double) {
        (store as Primitive64Store).add(row.toLong(), col.toLong(), value)
    }

    override fun add(other: Matrix): Matrix {
        val ojalgoMatrix = other as OjalgoMatrix
        return OjalgoMatrix(store.add(ojalgoMatrix.store))
    }

    override fun set(row: Int, col: Int, value: Double) {
        (store as Primitive64Store).set(row.toLong(), col.toLong(), value)
    }

    override fun negate(): Matrix {
        return OjalgoMatrix(store.negate())
    }

    override fun multiply(other: Matrix): Matrix {
        val ojalgoMatrix = other as OjalgoMatrix
        if (this.isNotEmpty() && ojalgoMatrix.isNotEmpty()) {
            return OjalgoMatrix(store.multiply(ojalgoMatrix.store))
        }
        return MatrixFactory.INSTANCE.zero(this.rowDim(), ojalgoMatrix.colDim())
    }

    override fun transpose(): Matrix {
        return OjalgoMatrix(store.transpose())
    }

    override fun rowDim(): Int {
        return store.rowDim
    }

    override fun colDim(): Int {
        return store.colDim
    }
}
