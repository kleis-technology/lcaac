package ch.kleis.lcaac.core.datasource.resilio_db

object ResilioDbConnectorKeys {
    const val RDB_CONNECTOR_NAME = "resilio_db"
    const val RDB_URL = "url"
    const val RDB_ACCESS_TOKEN = "accessToken"
    const val RDB_VERSION = "version"

    fun requiredOptionKeys() = setOf(
        RDB_URL, RDB_ACCESS_TOKEN, RDB_VERSION
    )
}
