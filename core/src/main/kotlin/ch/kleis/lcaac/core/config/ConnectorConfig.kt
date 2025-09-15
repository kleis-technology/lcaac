package ch.kleis.lcaac.core.config

import kotlinx.serialization.Serializable

@Serializable
data class ConnectorConfig(
    val name: String,
    val options: Map<String, String>,
    val cache: CacheConfig = CacheConfig(),
) {
    fun modifyOption(name: String, fn: (String) -> String) : ConnectorConfig =
        this.copy(
            options = this.options.mapValues {
                if (it.key == name) fn(it.value)
                else it.value
            }
        )
}

@Serializable
data class CacheConfig(
    val enabled: Boolean = false,
    val maxSize: Long = 1024,
    val maxRecordsPerCacheLine: Long = 8192,
)
