package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.EProcessTemplate

data class ProcessKey(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
){
    override fun toString(): String {
        return name + labels.entries.joinToString(", ") { "${it.key}=${it.value}" }
            .let { if (it.isNotEmpty()) "{$it}" else "" }
    }
}
typealias ProcessTemplateRegister<Q> = Register<ProcessKey, EProcessTemplate<Q>>
