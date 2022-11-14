package ch.kleis.lcaplugin.compute.matrix.impl

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
}
