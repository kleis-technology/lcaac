package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.QuantityOperations
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RdbClient<Q>(
    private val url: String,
    private val accessToken: String,
    private val ops: QuantityOperations<Q>,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {
    fun serverRack(
        rdbServerRack: RdbServerRack,
    ): List<ERecord<Q>> {
        val requestBody = rdbServerRack.json()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${this.url}/api/rack_server"))
            .header("Authorization", this.accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return emptyList()
    }
}
