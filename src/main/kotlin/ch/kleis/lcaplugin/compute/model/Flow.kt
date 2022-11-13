package ch.kleis.lcaplugin.compute.model

interface Flow : Indicator

data class IntermediaryFlow(val name: String) : Flow {
    override fun getUniqueId(): String {
        return name
    }
}

data class ElementaryFlow(val substance: String, val compartment: String?, val subcompartment: String?) : Flow {
    override fun getUniqueId(): String {
        return listOf(substance, compartment, subcompartment)
            .joinToString(":")
    }
}
