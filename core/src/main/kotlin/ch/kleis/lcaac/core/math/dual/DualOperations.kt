package ch.kleis.lcaac.core.math.dual

import ch.kleis.lcaac.core.math.Operations
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import kotlin.math.pow

class DualOperations(
    private val nParams: Int,
) : Operations<DualNumber, DualMatrix> {
    private val kernels = Kernels()

    fun basis(index: Int): DualNumber {
        with(kernels) {
            return DualNumber(
                0.0,
                ith(index, nParams)
            )
        }
    }

    override fun DualNumber.plus(other: DualNumber): DualNumber {
        return DualNumber(
            zeroth = this@plus.zeroth + other.zeroth,
            first = this@plus.first + other.first,
        )
    }

    override fun DualNumber.minus(other: DualNumber): DualNumber {
        return DualNumber(
            zeroth = this@minus.zeroth - other.zeroth,
            first = this@minus.first - other.first,
        )
    }

    /*
        d(a . b) = a . db + da . b
     */
    override fun DualNumber.times(other: DualNumber): DualNumber {
        return DualNumber(
            zeroth = this@times.zeroth * other.zeroth,
            first = this@times.zeroth * other.first + this@times.first * other.zeroth
        )
    }

    /*
        x = a/b
        means b . x = a

        b.dx + db.x = da
        dx = (da - db.(a/b))/b
     */
    override fun DualNumber.div(other: DualNumber): DualNumber {
        val a = this@div.zeroth
        val b = other.zeroth
        val da = this@div.first
        val db = other.first
        return DualNumber(
            zeroth = a / b,
            first = (da - db * (a / b)) / b
        )
    }


    /*
        d(a^n) = n * a^(n - 1) * da
     */
    override fun DualNumber.pow(other: Double): DualNumber {
        return DualNumber(
            zeroth = this@pow.zeroth.pow(other),
            first = other * this@pow.zeroth.pow(other - 1.0) * this@pow.first,
        )
    }

    override fun DualNumber.toDouble(): Double {
        return this.zeroth
    }

    override fun DualNumber.unaryMinus(): DualNumber {
        return DualNumber(
            zeroth = -this.zeroth,
            first = -this.first
        )
    }

    override fun pure(value: Double): DualNumber {
        return DualNumber(
            zeroth = value,
            first = mk.zeros(nParams),
        )
    }

    override fun zeros(rowDim: Int, colDim: Int): DualMatrix {
        return DualMatrix(
            zeroth = mk.zeros(rowDim, colDim),
            first = mk.zeros(rowDim, colDim, nParams),
        )
    }

    /*
        x = a/b
        means b . x = a

        b.dx + db.x = da
        dx = (da - db.x)/b
     */
    override fun DualMatrix.matDiv(other: DualMatrix): DualMatrix? {
        val a = this.zeroth
        val da = this.first
        val b = other.zeroth
        val db = other.first
        with(kernels) {
            val x = d22o0(a, b) ?: return null
            val dx = d22o1(da, b, db, x) ?: return null
            return DualMatrix(
                zeroth = x,
                first = dx,
            )
        }
    }

    /*
        d(a . b) = a . db + da . b
     */
    override fun DualMatrix.matMul(other: DualMatrix): DualMatrix {
        with(kernels) {
            val a = this@matMul.zeroth
            val b = other.zeroth
            val da = this@matMul.first
            val db = other.first
            return DualMatrix(
                zeroth = m22o0(a, b),
                first = m22o1(a, da, b, db),
            )
        }
    }

    override fun DualMatrix.set(row: Int, col: Int, value: DualNumber) {
        this.zeroth[row, col] = value.zeroth
        this.first[row, col] = value.first
    }

    override fun DualMatrix.get(row: Int, col: Int): DualNumber {
        return DualNumber(
            zeroth = this.zeroth[row, col],
            first = this.first[row, col].flatten() as D1Array<Double>,
        )
    }

    override fun DualMatrix.transpose(): DualMatrix {
        return DualMatrix(
            zeroth = this.zeroth.transpose(1, 0),
            first = this.first.transpose(1, 0, 2),
        )
    }

    override fun DualMatrix.negate(): DualMatrix {
        return DualMatrix(
            zeroth = -this.zeroth,
            first = -this.first,
        )
    }

    override fun DualMatrix.colDim(): Int {
        return this.zeroth.shape[1]
    }

    override fun DualMatrix.rowDim(): Int {
        return this.zeroth.shape[0]
    }
}
