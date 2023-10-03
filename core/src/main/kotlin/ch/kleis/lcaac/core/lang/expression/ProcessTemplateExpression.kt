package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics
import java.util.*

@optics
sealed interface ProcessTemplateExpression<Q> : Expression<Q> {
    companion object
}

@optics
data class EProcessTemplate<Q>(
    val params: Map<String, DataExpression<Q>> = emptyMap(),
    val locals: Map<String, DataExpression<Q>> = emptyMap(),
    val body: EProcess<Q>,
) : ProcessTemplateExpression<Q> {
    companion object
}

@optics
data class EProcessTemplateApplication<Q>(
    val template: EProcessTemplate<Q>,
    val arguments: Map<String, DataExpression<Q>> = emptyMap()
) : ProcessTemplateExpression<Q> {
    override fun hashCode(): Int =
        Objects.hash(template.body.name, template.body.labels, arguments)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        @Suppress("UNCHECKED_CAST")
        other as EProcessTemplateApplication<Q>

        if (template != other.template) return false
        if (arguments != other.arguments) return false

        return true
    }

    companion object
}

@optics
data class EProcessFinal<Q>(val expression: EProcess<Q>) : ProcessTemplateExpression<Q> {
    companion object
}


