package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorConfig.Companion.RESILIO_DB_ACCESS_TOKEN
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorConfig.Companion.RESILIO_DB_CONNECTOR_KEY_URL
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorConfig.Companion.RESILIO_DB_CONNECTOR_NAME

class ResilioDbConnectorConfig(
    val accessToken: String,
    val url: String = "https://db.resilio.tech",
) {
    companion object {
        const val RESILIO_DB_CONNECTOR_NAME = "resilio-db"
        const val RESILIO_DB_ACCESS_TOKEN = "accessToken"
        const val RESILIO_DB_CONNECTOR_KEY_URL = "url"
    }
}

fun ConnectorConfig.resilioDb(): ResilioDbConnectorConfig? {
    if (this.name != RESILIO_DB_CONNECTOR_NAME) {
        return null
    }
    val accessToken = this.options[RESILIO_DB_ACCESS_TOKEN]
        ?: throw IllegalArgumentException("Missing access token in Resilio DB config")
    val url = this.options[RESILIO_DB_CONNECTOR_KEY_URL]
        ?: "https://db.resilio.tech"
    return ResilioDbConnectorConfig(
        accessToken = accessToken,
        url = url,
    )
}
