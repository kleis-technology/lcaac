package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue

class InMemoryConnector<Q>(
    private val config: ConnectorConfig,
    private val content: Map<String, InMemoryDatasource<Q>>,
) : DataSourceConnector<Q> {
    override fun getName(): String {
        return InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME
    }

    override fun getConfig(): ConnectorConfig {
        return config
    }

    fun getSourceNames(): List<String> = content.keys.toList()

    override fun getAll(
        caller: DataSourceOperations<Q>,
        config: DataSourceConfig,
        source: DataSourceValue<Q>,
    ): Sequence<ERecord<Q>> {
        val sourceName = config.name
        val filter = source.filter
        val records = content[sourceName]
            ?.records
            ?.filter(applyFilter(filter))
            ?: emptyList()
        return records
            .map { it.toERecord() }
            .asSequence()
    }

    override fun getFirst(
        caller: DataSourceOperations<Q>,
        config: DataSourceConfig,
        source: DataSourceValue<Q>,
    ): ERecord<Q> {
        return getAll(caller, config, source).firstOrNull()
            ?: throw EvaluatorException("no record found in datasource '${config.name}' matching ${source.filter}")
    }
}

private fun <Q> applyFilter(
    filter: Map<String, DataValue<Q>>,
): (RecordValue<Q>) -> Boolean = { record ->
    filter.entries.all {
        val expected = it.value
        if (expected is StringValue) {
            when (val v = record.entries[it.key]) {
                is StringValue -> expected.s == v.s
                else -> throw EvaluatorException("invalid type for column '${it.key}': expected 'EStringLiteral', found '${v?.javaClass?.simpleName}'")
            }
        } else throw EvaluatorException("invalid matching condition $it")
    }
}
