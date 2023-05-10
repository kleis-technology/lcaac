package ch.kleis.lcaplugin.core.matrix.impl

interface Matrix {
    fun value(row: Int, col: Int): Double
    fun add(row: Int, col: Int, value: Double)
    fun set(row: Int, col: Int, value: Double)
    fun add(other: Matrix): Matrix
    fun negate(): Matrix
    fun sub(other: Matrix): Matrix {
        return add(other.negate())
    }

    fun multiply(other: Matrix): Matrix
    fun transpose(): Matrix

    fun rowDim(): Int
    fun colDim(): Int
    fun isEmpty(): Boolean {
        return rowDim() == 0 || colDim() == 0
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }
}
