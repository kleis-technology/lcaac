package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.misc.applyFilter
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.QuantityOperations

class InMemoryConnector<Q>(
    private val content: Map<String, List<ERecord<Q>>>,
    private val ops: QuantityOperations<Q>,
) : DataSourceConnector<Q> {
    override fun getName(): String {
        return InMemoryConnectorConfig.IN_MEMORY_CONNECTOR_NAME
    }

    override fun sumProduct(config: DataSourceConfig, source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        TODO("Not yet implemented")
    }

    private fun getRecordsOf(sourceName: String): List<ERecord<Q>> {
        return content[sourceName]
            ?: throw EvaluatorException("in_memory: unknown datasource '$sourceName'")
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceName = config.name
        val filter = source.filter
        val records = getRecordsOf(sourceName)
        return records.asSequence()
            .filter(applyFilter(source.config.name, ops, filter))
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(config, source).firstOrNull()
            ?: throw EvaluatorException("no record found in datasource '${config.name}' matching ${source.filter}")
    }
}
