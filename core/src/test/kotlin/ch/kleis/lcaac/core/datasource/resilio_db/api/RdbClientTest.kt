package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.*
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("Integration")
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
        val request = RdbRackServer(
            id = "server-01",
            modelName = "Foo",
            rackUnit = 3,
            cpuName = "Intel",
            cpuQuantity = 2,
            ramTotalSizeGb = 32.0,
            ssdTotalSizeGb = 2000.0,
            usage = RdbUsage(
                geography = "global",
                powerWatt = 100.0,
                durationOfUseHour = 1.0,
            ),
        )

        // when
        val response = client.serverRack(request)

        // then
        assert(response.size == RdbLcStep.entries.size)
    }

    @Test
    fun switch() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )
        val request = RdbSwitch(
            id = "server-01",
            cpuName = "Intel",
            cpuQuantity = 2,
            ramTotalSizeGb = 32.0,
        )

        // when
        val response = client.switch(request)

        // then
        assert(response.size == RdbLcStep.entries.size)
    }

    @Test
    fun userDevice_smartphone() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )
        val request = RdbUserDevice(
            id = "dev-01",
            deviceType = RdbUserDeviceType.SMARTPHONE,
            modelName = "android",
            usage = RdbUsage(
                geography = "global",
                powerWatt = 100.0,
                durationOfUseHour = 1.0,
            ),
        )

        // when
        val response = client.userDevice(request)

        // then
        assert(response.size == RdbLcStep.entries.size)
    }

    @Test
    fun userDevice_laptop() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )
        val request = RdbUserDevice(
            id = "dev-01",
            deviceType = RdbUserDeviceType.LAPTOP,
            modelName = "windows",
            usage = RdbUsage(
                geography = "global",
                powerWatt = 100.0,
                durationOfUseHour = 1.0,
            ),
        )

        // when
        val response = client.userDevice(request)

        // then
        assert(response.size == RdbLcStep.entries.size)
    }

    @Test
    fun userDevice_desktop() {
        // given
        val client = RdbClient(
            url = "https://db.resilio.tech",
            accessToken = System.getenv("RESILIO_DB_ACCESS_TOKEN"),
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )
        val request = RdbUserDevice(
            id = "dev-01",
            deviceType = RdbUserDeviceType.LAPTOP,
            modelName = "windows",
            usage = RdbUsage(
                geography = "global",
                powerWatt = 100.0,
                durationOfUseHour = 1.0,
            ),
        )

        // when
        val response = client.userDevice(request)

        // then
        assert(response.size == RdbLcStep.entries.size)
    }
}
