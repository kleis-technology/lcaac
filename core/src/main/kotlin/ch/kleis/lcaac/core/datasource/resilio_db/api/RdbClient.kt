package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.QuantityOperations
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class RdbServerRack(
    val id: String,
    val modelName: String,
    val rackUnit: Int,
    val cpuName: String,
    val cpuQuantity: Int,
    val ramTotalSizeGb: Float,
    val ssdTotalSizeGb: Float,
) {
    fun json(): String {
        val cpus = (1..cpuQuantity).joinToString(",") {
            """{ "name": "$cpuName" }"""
        }
        return """
        {
            "assembly": false,
            "data": [
                {
                    "name": "$id",
                    "wanted_name": "$modelName",
                    "cpus": [
                        $cpus
                    ],
                    "rams": [
                        { "size_gb": $ramTotalSizeGb }
                    ],
                    "ssd_disks": [
                        { "size_gb": $ssdTotalSizeGb }
                    ]
                }
            ]
        }
        """.trimIndent()
    }
}

@Suppress("EnumEntryName")
enum class RdbIndicator(private val rdbField: String) {
    ADPe("ADPe"),
    ADPf("ADPf"),
    AP("AP"),
    CTUe("CTUe"),
    CTUh_c("CTUh-c"),
    CTUh_nc("CTUh-nc"),
    Epf("Epf"),
    Epm("Epm"),
    Ept("Ept"),
    GWP("GWP"),
    GWPb("GWPb"),
    GWPf("GWPf"),
    GWPlu("GWPlu"),
    IR("IR"),
    LU("LU"),
    ODP("ODP"),
    PM("PM"),
    POCP("POCP"),
    WU("WU"),
    MIPS("MIPS"),
    TPE("TPE");

    companion object {
        fun fromRdbField(rdbField: String): RdbIndicator? {
            return entries.firstOrNull {
                it.rdbField == rdbField
            }
        }
    }

    fun <Q> asDataRef(): DataExpression<Q> =
        EDataRef(this.name)
}

class RdbClient<Q>(
    private val url: String,
    private val accessToken: String,
    private val ops: QuantityOperations<Q>,
) {
    private val httpClient = HttpClient.newHttpClient()

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
    }
}
