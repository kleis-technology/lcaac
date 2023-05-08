package ch.kleis.lcaplugin.language.psi.stub.substance

import java.io.Serializable

data class SubstanceKey(
        val fqn: String,
        val type: String,
        val compartment: String,
        val subCompartment: String?,
) : Serializable {
    private val localName = fqn.split(".").last()

    fun getPackageName(): String {
        val parts = fqn.split(".")
        return parts.take(parts.size - 1).joinToString(".")
    }

    fun getDisplayName(): String {
        val args = listOfNotNull(
                """compartment="$compartment"""",
                subCompartment?.let { """sub_compartment="$it"""" }
        ).joinToString()
        return "$localName($args)"
    }
}
