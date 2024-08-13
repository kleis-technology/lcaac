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
            val actualOptionKeys = config.options.keys
            val missingOptionsKeys = requiredOptionKeys.minus(actualOptionKeys)
            if (missingOptionsKeys.isNotEmpty()) {
                throw IllegalArgumentException("datasource '${config.name}': missing required options: $missingOptionsKeys")
            }


            val primaryKey = config.primaryKey
                ?: ResilioDbDataSourceKeys.defaultPrimaryKey
            val paramsFrom = config.options[ResilioDbDataSourceKeys.RDB_PARAMS_FROM]
                ?: throw IllegalArgumentException("datasource '${config.name}': missing option '${ResilioDbDataSourceKeys.RDB_PARAMS_FROM}'")
            val foreignKey = config.options[ResilioDbDataSourceKeys.RDB_FOREIGN_KEY]
                ?: ResilioDbDataSourceKeys.defaultForeignKey
            val rawEndpoint = config.options[ResilioDbDataSourceKeys.RDB_ENDPOINT]
                ?: throw IllegalArgumentException("datasource '${config.name}': missing option '${ResilioDbDataSourceKeys.RDB_ENDPOINT}'")
            val endpoint = rawEndpoint
                .let { SupportedEndpoint.from(it) }
                ?: throw IllegalArgumentException("connector '${config.name}': unsupported endpoint: $rawEndpoint")
            val lcStepKey = config.options[ResilioDbDataSourceKeys.RDB_LC_STEP_KEY]
                ?: ResilioDbDataSourceKeys.defaultLcStepKey
            val manufacturing = config.options[ResilioDbDataSourceKeys.RDB_MANUFACTURING]
                ?: ResilioDbDataSourceKeys.defaultManufacturing
            val transport = config.options[ResilioDbDataSourceKeys.RDB_TRANSPORT]
                ?: ResilioDbDataSourceKeys.defaultTransport
            val use = config.options[ResilioDbDataSourceKeys.RDB_USE]
                ?: ResilioDbDataSourceKeys.defaultUse
            val endOfLife = config.options[ResilioDbDataSourceKeys.RDB_END_OF_LIFE]
                ?: ResilioDbDataSourceKeys.defaultEndOfLife
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
