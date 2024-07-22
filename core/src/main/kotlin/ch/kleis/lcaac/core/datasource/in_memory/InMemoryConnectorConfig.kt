package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorConfig.Companion.IN_MEMORY_CONNECTOR_NAME
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord

class InMemoryConnectorConfig<Q>() {
    companion object {
        const val IN_MEMORY_CONNECTOR_NAME = "in_memory"
    }
}

fun <Q> ConnectorConfig.inMemory(): InMemoryConnectorConfig<Q>? {
    if (this.name != IN_MEMORY_CONNECTOR_NAME) {
        return null
    }
    return InMemoryConnectorConfig()
}
