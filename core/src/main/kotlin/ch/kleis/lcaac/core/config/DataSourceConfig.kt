package ch.kleis.lcaac.core.config

import arrow.typeclasses.Semigroup
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig
import kotlinx.serialization.Serializable

@Serializable
data class DataSourceConfig(
    val name: String,
    val connector: String? = CsvConnectorConfig.CSV_CONNECTOR_NAME,
    val location: String? = "$name.csv",
    val primaryKey: String? = "id",
    val options: Map<String, String> = emptyMap(),
) {
    companion object {
        fun completeWithDefaults(config: DataSourceConfig): DataSourceConfig {
            val defaultConfig = DataSourceConfig(
                name = config.name,
                connector = CsvConnectorConfig.CSV_CONNECTOR_NAME,
                location = "${config.name}.csv",
                primaryKey = "id",
                options = emptyMap(),
            )
            return with(merger(config.name)) {
                defaultConfig.combine(config)
            }
        }

        fun merger(name: String) = Semigroup<DataSourceConfig> { b ->
            if (b.name != name) {
                throw IllegalArgumentException("Cannot combine config for '$name' with config for '${b.name}'")
            }
            /*
                b overrides a's fields
             */
            DataSourceConfig(
                name = name,
                connector = b.connector ?: this.connector,
                location = b.location ?: this.location,
                primaryKey = b.primaryKey ?: this.primaryKey,
                options = b.options + this.options,
            )
        }
    }
}
