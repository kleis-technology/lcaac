package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorConfig.Companion.IN_MEMORY_CONNECTOR_NAME
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord

class InMemoryConnectorConfig<Q>(
    private val content: Map<String, List<ERecord<Q>>>
) {
    companion object {
        const val IN_MEMORY_CONNECTOR_NAME = "in_memory"
    }
    fun getRecordsOf(sourceName: String): List<ERecord<Q>> {
        return content[sourceName]
            ?: throw EvaluatorException("in_memory: unknown datasource '$sourceName'")
    }
}

fun <Q> ConnectorConfig.inMemory(
    content: Map<String, List<ERecord<Q>>>
): InMemoryConnectorConfig<Q>? {
    if (this.name != IN_MEMORY_CONNECTOR_NAME) {
        return null
    }
    return InMemoryConnectorConfig(content)
}
