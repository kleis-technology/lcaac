package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DummySourceOperations
import ch.kleis.lcaac.core.datasource.resilio_db.api.LcStepMapping
import ch.kleis.lcaac.core.datasource.resilio_db.api.RdbClient
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbRackServerDeserializer
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbSwitchDeserializer
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue

class ResilioDbConnector<Q>(
    private val config: ConnectorConfig,
    private val factory: ConnectorFactory<Q>,
    url: String,
    accessToken: String,
    private val rdbClientSupplier: (String, LcStepMapping) -> RdbClient<Q> =
        { primaryKey, lcStepMapping ->
            RdbClient(
                url = url,
                accessToken = accessToken,
                primaryKey = primaryKey,
                lcStepMapping = lcStepMapping,
                ops = factory.getQuantityOperations(),
            )
        }
) : DataSourceConnector<Q> {
    private val symbolTable = factory.getSymbolTable()
    private val ops = factory.getQuantityOperations()
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

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(config, source)
            .firstOrNull()
            ?: throw IllegalArgumentException("connector '${this.getName()}': no records found in datasource " +
                "'${source.config.name}'")
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val options = RDbDataSourceOptions.from(config)

        val auxiliaryDataSourceConfig = factory
            .getLcaacConfig()
            .getDataSource(options.paramsFrom)
            ?: throw IllegalArgumentException("connector '${this.getName()}': missing configuration for auxiliary datasource '${options.paramsFrom}'")
        val auxiliaryConnectorName = auxiliaryDataSourceConfig
            .connector
            ?: throw IllegalArgumentException("connector '${this.getName()}': missing connector in configuration of auxiliary datasource '${options.paramsFrom}'")
        val auxiliaryConnectorConfig = factory
            .getLcaacConfig()
            .getConnector(auxiliaryConnectorName)
            ?: throw IllegalArgumentException("connector '${this.getName()}': missing configuration for connector '$auxiliaryConnectorName'")
        val auxiliaryConnector = factory
            .buildOrNull(auxiliaryConnectorConfig)
            ?: throw IllegalArgumentException("connector '${this.getName()}': cannot build auxiliary connector '$auxiliaryConnectorName'")
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
                val auxiliaryRecords = auxiliaryConnector.getAll(
                    auxiliaryDataSourceConfig,
                    auxiliaryDataSource,
                )
                val requests = auxiliaryRecords.map {
                    deserializer.deserialize(it)
                }
                val rdbClient = rdbClientSupplier(options.primaryKey, options.lcStepMapping)
                val responses = requests
                    .flatMap {
                        rdbClient.serverRack(it)
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
                val auxiliaryRecords = auxiliaryConnector.getAll(
                    auxiliaryDataSourceConfig,
                    auxiliaryDataSource,
                )
                val requests = auxiliaryRecords.map {
                    deserializer.deserialize(it)
                }
                val rdbClient = rdbClientSupplier(options.primaryKey, options.lcStepMapping)
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
