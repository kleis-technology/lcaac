package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

enum class RdbUserDeviceType(val endpoint: String) {
    SMARTPHONE( "smartphone"),
    LAPTOP("laptop"),
    DESKTOP( "desktop");

    companion object {
        fun from(name: String): RdbUserDeviceType {
            val names = RdbUserDeviceType.entries.map { it.name }
            return RdbUserDeviceType.entries.firstOrNull { it.name.lowercase() == name.lowercase() }
                ?: throw IllegalArgumentException("invalid device type '$name', available device types are $names")
        }
    }
}

data class RdbUserDevice(
    val id: String,
    val deviceType: RdbUserDeviceType,
    val modelName: String,
    val usage: RdbUsage,
) {
    fun json(): String {
        return """
            {
                "assembly": false,
                "data": [
                    {
                        "wanted_name": "$id",
                        "name": "$modelName",
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
