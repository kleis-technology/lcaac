package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.runBlocking

class CachedConnector<Q>(
    private val inner: DataSourceConnector<Q>,
    private val maxSize: Long = 1024,
) : DataSourceConnector<Q> {
    private val sequenceCache = InMemoryKache<Pair<DataSourceConfig, DataSourceValue<Q>>, List<ERecord<Q>>>(
        maxSize = maxSize
    ) {
        strategy = KacheStrategy.LRU
    }
    private val rowCache = InMemoryKache<Pair<DataSourceConfig, DataSourceValue<Q>>, ERecord<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }

    override fun getName(): String = inner.getName()

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val result  = runBlocking {
            sequenceCache.getOrPut(config to source) {
                inner.getAll(config, source).toList()
            }
        }
        return result?.asSequence()
            ?: throw IllegalStateException("cannot fetch records from datasource $config")
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        val result  = runBlocking {
            rowCache.getOrPut(config to source) {
                inner.getFirst(config, source)
            }
        }
        return result
            ?: throw IllegalStateException("cannot fetch records from datasource $config")
    }
}
