package ch.kleis.lcaplugin.core.math.dual

import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.linalg.solve
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus

class Kernels {
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
     */

    /*
        solve b . x = a
            x = a/b = d22o0(a, b)
            dx = (da - db.x)/b = d22o1(da, b, db, x)
     */
    fun d22o0(a: D2Array<Double>, b: D2Array<Double>): D2Array<Double>? {
        return mk.linalg.solve(b, a)
    }

    fun d22o1(da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>, x: D2Array<Double>): D3Array<Double>? {
        val n = da - m32(db, x)
        val nFlattened = n.reshape(n.shape[0], n.shape[1] * n.shape[2])
        val dxFlattened = mk.linalg.solve(b, nFlattened)
        return dxFlattened.reshape(b.shape[1], n.shape[1], n.shape[2])
    }

    // a / b with rank a = 3 and rank b = 2
    private fun d23(a: D3Array<Double>, b: D2Array<Double>): D3Array<Double> {
        val aFlattened = a.reshape(a.shape[0], a.shape[1] * a.shape[2])
        val xFlattened = mk.linalg.solve(b, aFlattened)
        return xFlattened.reshape(b.shape[1], a.shape[1], a.shape[2])
    }

    /*
        mat mul
            x = a . b = m22o0(a, b)
            dx =  a . db + da . b = m22o1(a, da, b, db)
     */

    // x = a . b
    fun m22o0(a: D2Array<Double>, b: D2Array<Double>): D2Array<Double> {
        return mk.linalg.linAlgEx.dotMM(a, b)
    }

    // dx =  a . db + da . b
    fun m22o1(a: D2Array<Double>, da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>): D3Array<Double> {
        return m23(a, db) + m32(da, b)
    }


    private fun m23(a: D2Array<Double>, b: D3Array<Double>): D3Array<Double> {
        val bFlattened = b.reshape(b.shape[0], b.shape[1] * b.shape[2])
        val xFlattened = mk.linalg.linAlgEx.dotMM(a, bFlattened)
        return xFlattened.reshape(a.shape[0], b.shape[1], b.shape[2])
    }

    private fun m32(a: D3Array<Double>, b: D2Array<Double>): D3Array<Double> {
        val at = a.transpose(1, 0, 2)
        val bt = b.transpose(1, 0)
        val xt = m23(bt, at)
        return xt.transpose(1, 0, 2)
    }
}
