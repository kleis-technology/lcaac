package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
data class FromProcess(
    val ref: String,
    val arguments: Map<String, DataExpression>,
) {
    override fun toString(): String {
        return "from $ref$arguments"
    }

    companion object
}

@optics
data class MatchLabels(
    val elements: Map<String, StringExpression>,
) {
    override fun toString(): String {
        return "where $elements"
    }

    companion object
}
