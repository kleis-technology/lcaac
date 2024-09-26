package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbRackServer
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbSwitch
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbUserDevice
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.QuantityOperations
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RdbClient<Q>(
    private val url: String,
    private val accessToken: String,
    private val version: String,
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

    private val serverRackCache = InMemoryKache<RdbRackServer, List<ERecord<Q>>>(
        maxSize = 1024
    ) {
        strategy = KacheStrategy.LRU
    }
    private val switchCache = InMemoryKache<RdbSwitch, List<ERecord<Q>>>(
        maxSize = 1024
    ) {
        strategy = KacheStrategy.LRU
    }
    private val userDeviceCache = InMemoryKache<RdbUserDevice, List<ERecord<Q>>>(
        maxSize = 1024
    ) {
        strategy = KacheStrategy.LRU
    }

    fun serverRack(
        rdbServerRack: RdbRackServer,
    ): List<ERecord<Q>> {
        val result = runBlocking {
            serverRackCache.getOrPut(rdbServerRack) {
                request(rdbServerRack.id, "rack_server", rdbServerRack.json())
            }
        }
        return result
            ?: throw IllegalStateException("rdb client: could not send request $rdbServerRack")
    }

    fun switch(
        rdbSwitch: RdbSwitch,
    ): List<ERecord<Q>> {
        val result = runBlocking {
            switchCache.getOrPut(rdbSwitch) {
                request(rdbSwitch.id, "switch", rdbSwitch.json())
            }
        }
        return result
            ?: throw IllegalStateException("rdb client: could not send request $rdbSwitch")
    }

    fun userDevice(
        rdbUserDevice: RdbUserDevice,
    ): List<ERecord<Q>> {
        val result = runBlocking {
            userDeviceCache.getOrPut(rdbUserDevice) {
                request(rdbUserDevice.id, rdbUserDevice.deviceType.endpoint, rdbUserDevice.json())
            }
        }
        return result
            ?: throw IllegalStateException("rdb client: could not send request $rdbUserDevice")
    }

    private fun request(id: String, endpoint: String, requestBody: String): List<ERecord<Q>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${this.url}/api/${endpoint}/${version}"))
            .header("Authorization", this.accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() >= 400) {
            throw IllegalStateException("rdb client: error ${response.statusCode()}: ${response.body()}")
        }
        val rdbResponse = deserializer.decodeFromString(id, response.body())
        return listOf(
            rdbResponse.manufacturing,
            rdbResponse.transport,
            rdbResponse.use,
            rdbResponse.endOfLife,
        )
    }
}
