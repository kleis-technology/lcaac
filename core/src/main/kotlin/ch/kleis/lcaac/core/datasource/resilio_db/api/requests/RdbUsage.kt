package ch.kleis.lcaac.core.datasource.resilio_db.api.requests

data class RdbUsage(
    val geography: String,
    val powerWatt: Double,
    val durationOfUseHour: Double,
)
