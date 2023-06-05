package ch.kleis.lcaplugin.imports.model

import ch.kleis.lcaplugin.imports.ModelWriter

class SubstanceImported(
    val name: String, val type: String, val referenceUnit: String, val compartment: String,
    var subCompartment: String? = null,
    val impacts: MutableList<ImpactImported> = mutableListOf(),
    val meta: MutableMap<String, String?> = mutableMapOf(),
    pUid: String? = null
) {
    val uid: String

    init {
        uid = pUid?.let { ModelWriter.sanitizeAndCompact(it) } ?: ModelWriter.sanitizeAndCompact(name)
    }

    fun referenceUnitSymbol() = ModelWriter.sanitizeAndCompact(referenceUnit, false)
}

data class ImpactImported(val uid: String, val name: String, val value: Double, val unitSymbol: String) {
    constructor(value: Double, unitName: String, name: String) : this(
        ModelWriter.sanitizeAndCompact(name),
        name,
        value,
        ModelWriter.sanitizeAndCompact(unitName, toLowerCase = false)
    )
}