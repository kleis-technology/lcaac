package ch.kleis.lcaplugin.core.math.dual

import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array

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
        TODO()
    }

    fun d22o1(da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>, x: D2Array<Double>): D3Array<Double>? {
        TODO()
    }

    /*
        mat mul
            x = a . b = m22o0(a, b)
            dx =  a . db + da . b = m22o1(a, da, b, db)
     */

    fun m22o0(a: D2Array<Double>, b: D2Array<Double>): D2Array<Double> {
        TODO()
    }

    fun m22o1(a: D2Array<Double>, da: D3Array<Double>, b: D2Array<Double>, db: D3Array<Double>): D3Array<Double> {
        TODO()
    }
}
