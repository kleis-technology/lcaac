package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.runBlocking

class CachedConnector<Q>(
    private val inner: DataSourceConnector<Q>,
    private val maxSize: Long = 1024,
) : DataSourceConnector<Q> {
    private data class GetAllRequest<Q>(
        val config: DataSourceConfig,
        val source: DataSourceValue<Q>
    )
    private val getAllCache = InMemoryKache<GetAllRequest<Q>, List<ERecord<Q>>>(
        maxSize = maxSize
    ) {
        strategy = KacheStrategy.LRU
    }
    private data class GetFirstRequest<Q>(
        val config: DataSourceConfig,
        val source: DataSourceValue<Q>
    )
    private val getFirstCache = InMemoryKache<GetFirstRequest<Q>, ERecord<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
    private data class SumProductRequest<Q>(
        val config: DataSourceConfig,
        val source: DataSourceValue<Q>,
        val columns: List<String>,
    )
    private val sumProductCache = InMemoryKache<SumProductRequest<Q>, DataExpression<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }

    override fun getName(): String = inner.getName()
    override fun sumProduct(config: DataSourceConfig, source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val result = runBlocking { 
            sumProductCache.getOrPut(SumProductRequest(config, source, columns)) {
                inner.sumProduct(config, source, columns)
            }
        }
        return result
            ?: throw EvaluatorException("cannot compute sum product $columns from datasource $config")
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val result  = runBlocking {
            getAllCache.getOrPut(GetAllRequest(config, source)) {
                inner.getAll(config, source).toList()
            }
        }
        return result?.asSequence()
            ?: throw EvaluatorException("cannot fetch records from datasource $config")
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        val result  = runBlocking {
            getFirstCache.getOrPut(GetFirstRequest(config, source)) {
                inner.getFirst(config, source)
            }
        }
        return result
            ?: throw EvaluatorException("cannot fetch records from datasource $config")
    }
}
