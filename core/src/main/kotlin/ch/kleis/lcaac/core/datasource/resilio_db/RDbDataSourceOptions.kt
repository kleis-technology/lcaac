package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.resilio_db.api.LcStepMapping
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint

data class RDbDataSourceOptions(
    val primaryKey: String,
    val paramsFrom: String,
    val foreignKey: String,
    val endpoint: SupportedEndpoint,
    val lcStepMapping: LcStepMapping,
) {
    companion object {
        fun from(config: DataSourceConfig): RDbDataSourceOptions {
            val requiredOptionKeys = ResilioDbDataSourceKeys.requiredOptionKeys()
            val actualOptionKeys = requiredOptionKeys
                .mapNotNull { config.options[it] }
                .toSet()
            if (requiredOptionKeys.size > actualOptionKeys.size) {
                val missingOptionsKeys = requiredOptionKeys.minus(actualOptionKeys)
                throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': missing required options: $missingOptionsKeys")
            }

            val primaryKey = config.options[ResilioDbDataSourceKeys.RDB_PRIMARY_KEY]!!
            val paramsFrom = config.options[ResilioDbDataSourceKeys.RDB_PARAMS_FROM]!!
            val foreignKey = config.options[ResilioDbDataSourceKeys.RDB_FOREIGN_KEY]!!
            val rawEndpoint = config.options[ResilioDbDataSourceKeys.RDB_ENDPOINT]
            val endpoint = rawEndpoint!!
                .let { SupportedEndpoint.from(it) }
                ?: throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': unsupported " +
                    "endpoint: $rawEndpoint")
            val lcStepKey = config.options[ResilioDbDataSourceKeys.RDB_LC_STEP_KEY]!!
            val manufacturing = config.options[ResilioDbDataSourceKeys.RDB_MANUFACTURING]!!
            val transport = config.options[ResilioDbDataSourceKeys.RDB_TRANSPORT]!!
            val use = config.options[ResilioDbDataSourceKeys.RDB_USE]!!
            val endOfLife = config.options[ResilioDbDataSourceKeys.RDB_END_OF_LIFE]!!
            return RDbDataSourceOptions(
                primaryKey = primaryKey,
                paramsFrom = paramsFrom,
                foreignKey = foreignKey,
                endpoint = endpoint,
                lcStepMapping = LcStepMapping(
                    key = lcStepKey,
                    manufacturing = manufacturing,
                    transport = transport,
                    use = use,
                    endOfLife = endOfLife,
                )
            )
        }
    }
}
