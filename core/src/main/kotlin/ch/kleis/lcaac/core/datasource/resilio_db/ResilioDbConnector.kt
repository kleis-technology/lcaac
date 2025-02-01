package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DataSourceOperationsWithConfig
import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.datasource.resilio_db.api.RdbClientPool
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbRackServerDeserializer
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbSwitchDeserializer
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbUserDeviceDeserializer
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

class ResilioDbConnector<Q>(
    private val config: ConnectorConfig,
    symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
    url: String,
    accessToken: String,
    version: String,
    private val rdbClientPool: RdbClientPool<Q> = RdbClientPool(
        url = url,
        accessToken = accessToken,
        version = version,
        ops = ops,
    )
) : DataSourceConnector<Q> {
    private var hits: Int = 0

    private val dataReducer = DataExpressionReducer(
        dataRegister = symbolTable.data,
        dataSourceRegister = symbolTable.dataSources,
        ops = ops,
        sourceOps = DummySourceOperations(),
    )

    private fun localEval(data: DataExpression<Q>): DataValue<Q> {
        return with(ToValue(ops)) {
            dataReducer.reduce(data).toValue()
        }
    }


    override fun getName(): String {
        return ResilioDbConnectorKeys.RDB_CONNECTOR_NAME
    }

    override fun getConfig(): ConnectorConfig {
        return config
    }

    override fun getFirst(caller: DataSourceOperationsWithConfig<Q>, config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(caller, config, source)
            .firstOrNull()
            ?: throw IllegalArgumentException("connector '${this.getName()}': no records found in datasource " +
                "'${source.config.name}'")
    }

    override fun getAll(caller: DataSourceOperationsWithConfig<Q>, config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        hits += 1
        val options = RDbDataSourceOptions.from(config)
        val rdbClient = rdbClientPool.get(options.primaryKey, options.lcStepMapping)

        val auxiliaryDataSourceConfig = caller.getConfig()
            .getDataSource(options.paramsFrom)
            ?: throw IllegalArgumentException("connector '${this.getName()}': missing configuration for auxiliary datasource '${options.paramsFrom}'")
        val auxiliaryFilter = source.filter[options.primaryKey]
            ?.let { joinKeyValue ->
                mapOf(
                    options.foreignKey to joinKeyValue
                )
            } ?: emptyMap()

        when (options.endpoint) {
            SupportedEndpoint.RACK_SERVER -> {
                val deserializer = RdbRackServerDeserializer(
                    options.primaryKey,
                    ops,
                    this::localEval
                )
                val auxiliaryDataSource = DataSourceValue(
                    config = auxiliaryDataSourceConfig,
                    schema = deserializer.schema(),
                    filter = auxiliaryFilter
                )
                val auxiliaryRecords = caller.getAll(
                    auxiliaryDataSource,
                )
                val requests = auxiliaryRecords.map {
                    deserializer.deserialize(it)
                }
                val responses = requests
                    .flatMap {
                        rdbClient.serverRack(it)
                    }
                return responses.filter(applyFilter(source.filter))
            }

            SupportedEndpoint.USER_DEVICE -> {
                val deserializer = RdbUserDeviceDeserializer(
                    options.primaryKey,
                    ops,
                    this::localEval,
                )
                val auxiliaryDataSource = DataSourceValue(
                    config = auxiliaryDataSourceConfig,
                    schema = deserializer.schema(),
                    filter = auxiliaryFilter
                )
                val auxiliaryRecords = caller.getAll(
                    auxiliaryDataSource,
                )
                val requests = auxiliaryRecords.map {
                    deserializer.deserialize(it)
                }
                val responses = requests
                    .flatMap {
                        rdbClient.userDevice(it)
                    }
                return responses.filter(applyFilter(source.filter))
            }

            SupportedEndpoint.SWITCH -> {
                val deserializer = RdbSwitchDeserializer(
                    options.primaryKey,
                    ops,
                    this::localEval
                )
                val auxiliaryDataSource = DataSourceValue(
                    config = auxiliaryDataSourceConfig,
                    schema = deserializer.schema(),
                    filter = auxiliaryFilter
                )
                val auxiliaryRecords = caller.getAll(
                    auxiliaryDataSource,
                )
                val requests = auxiliaryRecords.map {
                    deserializer.deserialize(it)
                }
                val responses = requests
                    .flatMap {
                        rdbClient.switch(it)
                    }
                return responses.filter(applyFilter(source.filter))
            }
        }
    }
}

private fun <Q> applyFilter(
    filter: Map<String, DataValue<Q>>,
): (ERecord<Q>) -> Boolean = { record ->
    filter.entries.all {
        when (val expected = it.value) {
            is StringValue -> when (val v = record.entries[it.key]) {
                is EStringLiteral -> expected.s == v.value
                else -> throw EvaluatorException("invalid type for column '${it.key}': expected 'EStringLiteral', found '${v?.javaClass?.simpleName}'")
            }

            else -> throw EvaluatorException("invalid matching condition $it")
        }
    }
}
