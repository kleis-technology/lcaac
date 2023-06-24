package ch.kleis.lcaplugin.core.matrix.impl

interface Matrix {
    fun value(row: Int, col: Int): Double
    fun add(row: Int, col: Int, value: Double) {
        set(row, col, value(row, col) + value)
    }
    fun set(row: Int, col: Int, value: Double)

    fun negate(): Matrix
    fun transpose(): Matrix

    fun rowDim(): Int
    fun colDim(): Int
}
