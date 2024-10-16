package ch.kleis.lcaac.core.datasource.resilio_db.api

enum class SupportedEndpoint(private val value: String) {
    RACK_SERVER("rack_server"),
    SWITCH("switch"),
    USER_DEVICE("user_device");

    companion object {
        fun from(endpoint: String): SupportedEndpoint? = entries
            .firstOrNull { it.value == endpoint }
    }
}
