package ch.kleis.lcaplugin.core.math.dual

import com.intellij.openapi.diagnostic.Logger
import org.ejml.data.DMatrixSparseCSC
import org.ejml.data.MatrixType
import org.ejml.simple.SimpleMatrix
import org.ejml.sparse.csc.CommonOps_DSCC
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.forEachMultiIndexed
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus

class Kernels {
    private val LOG = Logger.getInstance(Kernels::class.java)

    /*
        Basics
     */

    fun ith(index: Int, dim: Int): D1Array<Double> {
        return mk.d1array(dim) {
            if (it == index) 1.0 else 0.0
        }
    }

    /*
         Specials
         Note:
            One cannot use mk.linalg.solve when running plugin, ClassDefNotFound error (sic).
            Hence, the use of EJML.
     */

    private fun d2Ejml(m: D2Array<Double>): SimpleMatrix {
        val (rows, cols) = m.shape
        val r = SimpleMatrix(rows, cols, MatrixType.DSCC)
        m.forEachMultiIndexed { index, d ->
            val (i, j) = index
            r.set(i, j, d)
        }
        return r
    }

    private fun ejmlD2(m: SimpleMatrix): D2Array<Double> {
        val (rows, cols) = Pair(m.numRows, m.numCols)
        val r = mk.zeros<Double>(rows, cols)
        val iterator = m.getMatrix<DMatrixSparseCSC>().createCoordinateIterator()
        while (iterator.hasNext()) {
            val v = iterator.next()
            r[v.row, v.col] = v.value
        }
        return r
    }


    /*
        solve b . x = a
            x = a/b = d22o0(a, b)
            dx = (da - db.x)/b = d22o1(da, b, db, x)
     */
    fun d22o0(a: D2Array<Double>, b: D2Array<Double>): D2Array<Double>? {
        val aa = d2Ejml(a).dscc
        val bb = d2Ejml(b).dscc
        val xx = DMatrixSparseCSC(bb.numCols, aa.numCols)
        return if (CommonOps_DSCC.solve(bb, aa, xx)) {
            LOG.info("End solving with result(${xx.numRows}, ${xx.numCols})")
            ejmlD2(SimpleMatrix.wrap(xx))
        } else {
            null
        }
    }

    fun d22o1(da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>, x: D2Array<Double>): D3Array<Double>? {
        val n = da - m32(db, x)
        val nFlattened = n.reshape(n.shape[0], n.shape[1] * n.shape[2])
        val dxFlattened = d22o0(nFlattened, b) ?: return null
        return dxFlattened.reshape(b.shape[1], n.shape[1], n.shape[2])
    }

    /*
        mat mul
            x = a . b = m22o0(a, b)
            dx =  a . db + da . b = m22o1(a, da, b, db)
     */

    // x = a . b
    fun m22o0(a: D2Array<Double>, b: D2Array<Double>): D2Array<Double> {
        val aa = d2Ejml(a).dscc
        val bb = d2Ejml(b).dscc
        val xx = DMatrixSparseCSC(aa.numRows, bb.numCols)
        CommonOps_DSCC.mult(aa, bb, xx)
        return ejmlD2(SimpleMatrix.wrap(xx))
    }

    // dx =  a . db + da . b
    fun m22o1(a: D2Array<Double>, da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>): D3Array<Double> {
        return m23(a, db) + m32(da, b)
    }


    private fun m23(a: D2Array<Double>, b: D3Array<Double>): D3Array<Double> {
        val bFlattened = b.reshape(b.shape[0], b.shape[1] * b.shape[2])
        val xFlattened = m22o0(a, bFlattened)
        return xFlattened.reshape(a.shape[0], b.shape[1], b.shape[2])
    }

    private fun m32(a: D3Array<Double>, b: D2Array<Double>): D3Array<Double> {
        val at = a.transpose(1, 0, 2)
        val bt = b.transpose(1, 0)
        val xt = m23(bt, at)
        return xt.transpose(1, 0, 2)
    }
}
