package ch.kleis.lcaac.core.config

import kotlinx.serialization.Serializable

@Serializable
data class ConnectorConfig(
    val name: String,
    val cache: CacheConfig,
    val options: Map<String, String>,
)

@Serializable
data class CacheConfig(
    val enabled: Boolean = false,
    val maxSize: Long = 1024,
)
