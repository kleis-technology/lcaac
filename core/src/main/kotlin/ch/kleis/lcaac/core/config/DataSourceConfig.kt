package ch.kleis.lcaac.core.config

import arrow.typeclasses.Semigroup
import kotlinx.serialization.Serializable

@Serializable
data class DataSourceConfig(
    val name: String,
    val connector: String? = null,
    val location: String? = null,
    val primaryKey: String? = null,
    val options: Map<String, String> = emptyMap(),
) {
    companion object {
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
