package ch.kleis.lcaplugin.imports.model

import ch.kleis.lcaplugin.imports.ModelWriter

class SubstanceImported(
    val name: String, val type: String, val referenceUnit: String, val compartment: String,
    var subCompartment: String? = null
) {

    var impacts: MutableList<ImpactImported> = mutableListOf()
    var meta: MutableMap<String, String?> = mutableMapOf()
    val uid = ModelWriter.sanitizeAndCompact(name)
}

data class ImpactImported(val value: Double, val unit: String, val uid: String)