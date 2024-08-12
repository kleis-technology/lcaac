package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test

//@Tag("Integration")
class RdbClientTest {
    private val lcStepMapping = LcStepMapping(
        key = "lc_step",
        manufacturing = "manufacturing",
        transport = "transport",
        use = "use",
        endOfLife = "end-of-life",
    )

    @Test
    fun serverRack() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
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
