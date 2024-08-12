package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag

//@Tag("Integration")
class RdbClientTest {

    @Test
    fun serverRack() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            ops = BasicOperations,
        )
        val request = RdbServerRack(
            id = "server-01",
            modelName = "Foo",
            rackUnit = 3,
            cpuName = "Intel",
            cpuQuantity = 2,
            ramTotalSizeGb = 32.0,
            ssdTotalSizeGb = 2000.0,
        )

        // when
        val response = client.serverRack(request)

        // then
        println()
    }
}
