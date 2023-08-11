package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
data class FromProcess(
    val name: String,
    val matchLabels: MatchLabels,
    val arguments: Map<String, DataExpression> = emptyMap(),
) {
    override fun toString(): String {
        return "from $name$matchLabels$arguments"
    }

    companion object
}

@optics
data class MatchLabels(
    val elements: Map<String, DataExpression>,
) {
    override fun toString(): String {
        return if (elements.isEmpty()) "" else "$elements"
    }

    companion object {
        val EMPTY = MatchLabels(emptyMap())
    }
}
