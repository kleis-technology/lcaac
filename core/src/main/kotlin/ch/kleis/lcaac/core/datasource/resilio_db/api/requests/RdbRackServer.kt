package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

data class RdbRackServer(
    val id: String,
    val modelName: String,
    val rackUnit: Int,
    val cpuName: String,
    val cpuQuantity: Int,
    val ramTotalSizeGb: Double,
    val ssdTotalSizeGb: Double,
    val usage: RdbUsage,
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
                    "wanted_name": "$id",
                    "name": "$modelName",
                    "cpus": [
                        $cpus
                    ],
                    "rams": [
                        { "size_gb": $ramTotalSizeGb }
                    ],
                    "ssd_disks": [
                        { "size_gb": $ssdTotalSizeGb }
                    ],
                    "usage": {
                        "geography": "${usage.geography}",
                        "power_watt": ${usage.powerWatt},
                        "duration_of_use_hour": ${usage.durationOfUseHour}
                    }
                }
            ]
        }
        """.trimIndent()
    }
}
