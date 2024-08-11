package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy

class ConnectorCache<Q>(
    private val maxSize: Long
) {
    val getAllCache = InMemoryKache<GetAllRequest<Q>, List<ERecord<Q>>>(
        maxSize = maxSize
    ) {
        strategy = KacheStrategy.LRU
    }
    val getFirstCache = InMemoryKache<GetFirstRequest<Q>, ERecord<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
    val sumProductCache = InMemoryKache<SumProductRequest<Q>, DataExpression<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
}

data class GetAllRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

data class GetFirstRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

data class SumProductRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>,
    val columns: List<String>,
)


