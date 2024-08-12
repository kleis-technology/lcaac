package ch.kleis.lcaac.core.datasource.resilio_db.api

data class RdbServerRack(
    val id: String,
    val modelName: String,
    val rackUnit: Int,
    val cpuName: String,
    val cpuQuantity: Int,
    val ramTotalSizeGb: Double,
    val ssdTotalSizeGb: Double,
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
