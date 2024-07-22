package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.misc.reduceSumProduct
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.QuantityOperations

class OverriddenDataSourceOperations<Q>(
    private val content: Map<String, List<ERecord<Q>>>,
    private val ops: QuantityOperations<Q>,
    private val innerSourceOps: DataSourceOperations<Q>,
) : DataSourceOperations<Q> {
    private val inMemoryConnector = InMemoryConnector(content, ops)

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        return if (content.containsKey(source.config.name))
            inMemoryConnector.getFirst(source.config, source)
        else innerSourceOps.getFirst(source)
    }

    override fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        return if (content.containsKey(source.config.name))
            inMemoryConnector.getAll(source.config, source)
        else innerSourceOps.getAll(source)
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        return if (!content.containsKey(source.config.name))
            innerSourceOps.sumProduct(source, columns)
        else reduceSumProduct(
            source.config.name,
            ops,
            this,
            inMemoryConnector.getAll(source.config, source),
            columns,
        )
    }
}
