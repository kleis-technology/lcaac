package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.SubstanceType

data class SubstanceKey(
    val name: String,
    val type: SubstanceType? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
) {
    override fun toString(): String {
        return name + listOfNotNull(
            type?.let { "type=$it" },
            compartment?.let { "compartment=$it" },
            subCompartment?.let { "sub_compartment=$it" },
        ).joinToString(", ").let { if (it.isNotEmpty()) "($it)"  else ""}
    }
}
typealias SubstanceCharacterizationRegister<Q> = Register<SubstanceKey, ESubstanceCharacterization<Q>>
