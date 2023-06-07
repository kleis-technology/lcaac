package ch.kleis.lcaplugin.language.psi.stub.process

import java.io.Serializable

data class ProcessKey(
    val fqn: String,
    val labels: Map<String, String>,
) : Serializable {
    private val localName = fqn.split(".").last()

    fun getPackageName(): String {
        val parts = fqn.split(".")
        return parts.take(parts.size - 1).joinToString(".")
    }

    fun getDisplayName(): String {
        val matchLabels = labels
            .map {
                "${it.key} = \"${it.value}\""
            }
            .joinToString(", ")
        if (matchLabels.isBlank()) {
            return localName
        }
        return "$localName match ($matchLabels)"
    }
}
