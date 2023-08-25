package ch.kleis.lcaplugin.core.math.basic

data class BasicNumber(val value: Double) {
    override fun toString(): String {
        return "$value"
    }
}
