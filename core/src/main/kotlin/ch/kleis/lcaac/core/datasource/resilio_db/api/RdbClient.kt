package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbRackServer
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbSwitch
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.QuantityOperations
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RdbClient<Q>(
    private val url: String,
    private val accessToken: String,
    primaryKey: String,
    lcStepMapping: LcStepMapping,
    ops: QuantityOperations<Q>,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {
    private val deserializer = RdbResponseDeserializer(
        primaryKey = primaryKey,
        lcStepMapping = lcStepMapping,
        ops = ops,
    )

    fun serverRack(
        rdbServerRack: RdbRackServer,
    ): List<ERecord<Q>> {
        return request(rdbServerRack.id, "rack_server", rdbServerRack.json())
    }

    fun switch(
        rdbSwitch: RdbSwitch,
    ): List<ERecord<Q>> {
        return request(rdbSwitch.id, "switch", rdbSwitch.json())
    }

    private fun request(id: String, endpoint: String, requestBody: String): List<ERecord<Q>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${this.url}/api/${endpoint}"))
            .header("Authorization", this.accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val rdbResponse = deserializer.decodeFromString(id, response.body())
        return listOf(
            rdbResponse.manufacturing,
            rdbResponse.transport,
            rdbResponse.use,
            rdbResponse.endOfLife,
        )
    }
}
