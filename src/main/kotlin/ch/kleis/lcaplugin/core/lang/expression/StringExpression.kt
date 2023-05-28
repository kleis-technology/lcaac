package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics


@optics
data class EStringLiteral(val value: String): StringExpression {
    override fun toString(): String {
        return value
    }

    companion object
}

@optics
data class EStringRef(val name: String) : StringExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String {
        return name
    }

    companion object
}
