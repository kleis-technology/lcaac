package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.runBlocking

class SourceOpsCache<Q>(
    maxSize: Long,
    private val maxRecordsPerCacheLine: Long,
) {
    private val getAllCache = InMemoryKache<GetAllRequest<Q>, List<ERecord<Q>>>(
        maxSize = maxSize
    ) {
        strategy = KacheStrategy.LRU
    }
    private val getFirstCache = InMemoryKache<GetFirstRequest<Q>, ERecord<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
    private val sumProductCache = InMemoryKache<SumProductRequest<Q>, DataExpression<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }

    private fun <K : Any, V : Any> InMemoryKache<K, V>.safeGetOrPut(key: K, fn: (K) -> V): V {
        return when (val found = getIfAvailable(key)) {
            null -> {
                val value = fn(key)
                runBlocking { put(key, value) }
                return value
            }

            else -> found
        }
    }

    fun recordGetFirst(
        config: DataSourceConfig,
        source: DataSourceValue<Q>,
        fn: () -> ERecord<Q>,
    ): ERecord<Q> {
        val key = GetFirstRequest(config, source)
        val result = getFirstCache.safeGetOrPut(key) { fn() }
        return result
    }

    fun recordGetAll(
        config: DataSourceConfig,
        source: DataSourceValue<Q>,
        fn: () -> List<ERecord<Q>>,
    ): List<ERecord<Q>> {
        val result = runBlocking {
            val key = GetAllRequest(config, source)
            val value = getAllCache.getIfAvailable(key)
            if (value != null) value
            else {
                val records = fn()
                if (records.size < maxRecordsPerCacheLine) {
                    getAllCache.put(key, records)
                }
                records
            }
        }
        return result
    }

    fun recordSumProduct(
        config: DataSourceConfig,
        source: DataSourceValue<Q>,
        columns: List<String>,
        fn: () -> DataExpression<Q>,
    ): DataExpression<Q> {
        val key = SumProductRequest(config, source, columns)
        val result = sumProductCache.safeGetOrPut(key) { fn() }
        return result
    }
}

private data class GetAllRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

private data class GetFirstRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

private data class SumProductRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>,
    val columns: List<String>,
)


