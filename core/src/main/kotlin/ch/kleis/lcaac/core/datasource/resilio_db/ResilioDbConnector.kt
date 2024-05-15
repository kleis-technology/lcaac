package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class ResilioDbConnector<Q>(
    private val factory: ConnectorFactory<Q>,
    private val config: ResilioDbConnectorConfig,
) : DataSourceConnector<Q> {
    override fun getName(): String {
        return ResilioDbConnectorConfig.RESILIO_DB_CONNECTOR_NAME
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        //
        val paramsFrom = config.options[KEY_PARAMS_FROM]
            ?: throw IllegalArgumentException("Missing '$KEY_PARAMS_FROM'")
        val foreignKey = config.options[KEY_FOREIGN_KEY]
            ?: throw IllegalArgumentException("Missing '$KEY_FOREIGN_KEY'")
        val paramsId = source.filter[foreignKey]
            ?.let {
                when (it) {
                    is StringValue -> it.s
                    else -> TODO()
                }
            }
            ?: TODO()

        val paramsDataSource = factory.getLcaacConfig().getDataSource(paramsFrom)
            ?: TODO()
        val paramsConnectorConfig = paramsDataSource.connector
            ?.let { factory.getLcaacConfig().getConnector(it) }
            ?: TODO()
        val connector = factory.buildOrNull(paramsConnectorConfig)
            ?: TODO()

        val params = connector.getFirst(
            paramsDataSource,
            DataSourceValue(
                paramsDataSource,
                schema = mapOf(
                    PARAM_KEY_ID to StringValue("id"),
                    PARAM_KEY_DESCRIPTION to StringValue("description"),
                    PARAM_KEY_ENDPOINT to StringValue("endpoint"),
                ),
                filter = mapOf(paramsDataSource.primaryKey!! to StringValue(paramsId)),
            ),
        )

        // build request

        val lcStep = source.filter[SOURCE_KEY_LC_STEP]
            ?.let {
                when (it) {
                    is StringValue -> it.s
                    else -> TODO()
                }
            }
            ?: TODO()

        val httpClient = HttpClient.newHttpClient()
        val wantedName = params.entries[PARAM_KEY_ID]
            ?.let {
                when (it) {
                    is EStringLiteral -> it.value
                    else -> TODO()
                }
            } ?: TODO()

        val requestBody = """
                {
                  "assembly": false,
                  "data": [
                      {
                        "name": "${params.entries[PARAM_KEY_DESCRIPTION]}",
                        "wanted_name": "$wantedName"
                      }
                  ]
                }
            """.trimIndent()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${this.config.url}/api/${params.entries[PARAM_KEY_ENDPOINT]}"))
            .header("Authorization", this.config.accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return decodeRecordFromString(wantedName, lcStep, source.schema, response.body())
    }

    private fun decodeRecordFromString(
        id: String,
        lcStep: String,
        schema: Map<String, DataValue<Q>>,
        body: String): ERecord<Q> {
        val root = Json.decodeFromString<JsonObject>(body)
        val results = root["results"] as JsonObject
        val impacts = results[id] as JsonObject
        val perLcStep = impacts["per_lc_step"] as JsonObject
        val rawEntries = perLcStep[lcStep] as JsonObject
        val entries = rawEntries
            .mapKeys {
                when (it.key) {
                    "CTUh-c" -> "CTUh_c"
                    "CTUh-nc" -> "CTUh_nc"
                    else -> it.key
                }
            }
            .mapValues { Json.decodeFromJsonElement<Double>(it.value) }
            .mapValues {
                when(it.key) {
                    "GWP" -> with(factory.getQuantityOperations()) {
                        EQuantityScale(pure(it.value), EDataRef("kg_CO2_Eq"))
                    }
                    else -> TODO()
                }
            }


        return ERecord(emptyMap())
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val KEY_PARAMS_FROM: String = "paramsFrom"
        private const val KEY_FOREIGN_KEY: String = "foreignKey"
        private const val SOURCE_KEY_LC_STEP: String = "lc_step"
        private const val PARAM_KEY_ID: String = "id"
        private const val PARAM_KEY_DESCRIPTION: String = "description"
        private const val PARAM_KEY_ENDPOINT: String = "endpoint"
    }
}
