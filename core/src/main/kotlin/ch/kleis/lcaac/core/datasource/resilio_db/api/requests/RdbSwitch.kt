package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

data class RdbSwitch(
    val id: String,
    val cpuName: String,
    val cpuQuantity: Int,
    val ramTotalSizeGb: Double,
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
                    "cpus": [
                        $cpus
                    ],
                    "rams": [
                        { "size_gb": $ramTotalSizeGb }
                    ]
                }
            ]
        }
        """.trimIndent()
    }
}
