package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.CacheConfig
import ch.kleis.lcaac.core.config.ConnectorConfig

object InMemoryConnectorKeys {
    const val IN_MEMORY_CONNECTOR_NAME = "in_memory"

    fun defaultConfig(
        cacheEnabled: Boolean = true,
        cacheSize: Long = 1024,
    ): ConnectorConfig =
        ConnectorConfig(
            name = IN_MEMORY_CONNECTOR_NAME,
            cache = CacheConfig(
                enabled = cacheEnabled,
                maxSize = cacheSize,
            ),
            options = emptyMap(),
        )
}
